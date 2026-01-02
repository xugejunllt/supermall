package com.lanf.goods.service.goods.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.goods.mapper.StockMapper;
import com.lanf.goods.model.bo.SkuCodeStockBO;
import com.lanf.goods.model.bo.StockSaveOrUpdateBO;
import com.lanf.goods.model.entity.StockDO;
import com.lanf.goods.model.entity.UserStockSyncRecordDO;
import com.lanf.goods.service.goods.IStockService;

import com.lanf.goods.service.goods.IUserStockSyncRecordService;
import com.lanf.rocketmq.model.message.UserStockAddMsg;
import com.lanf.rocketmq.model.message.UserStockMsg;
import com.lanf.constant.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 库存 服务实现类
 * </p>
 *
 * @author jarven
 * @since 2025-11-29
 */
@Slf4j
@Service
public class StockServiceImpl extends ServiceImpl<StockMapper, StockDO> implements IStockService {

    @Autowired
    private IUserStockSyncRecordService userStockFlowService;

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void addUserStock(UserStockAddMsg message) {
        log.info("新增用户库存");
        StockSaveOrUpdateBO stockSaveOrUpdateBO = buildStockSaveOrUpdate(message.getUserStockList());
        List<UserStockSyncRecordDO> stockFlowDOList = buildUserStockFlowDO(message);
        List<StockDO> stockSave = stockSaveOrUpdateBO.getStockSave();
        List<StockDO> stockUpdate = stockSaveOrUpdateBO.getStockUpdate();
        if (!stockUpdate.isEmpty()) {

            log.info("批量更新");
            //乐观锁更新 更新商品库存
            stockUpdate.forEach(a -> {
                boolean update = this.lambdaUpdate().
                        eq(StockDO::getId, a.getId()).
                        eq(StockDO::getVersion, a.getVersion()).
                        set(StockDO::getTotalStock, a.getTotalStock()).
                        set(StockDO::getVersion, a.getVersion() + 1).
                        update();
                if (!update) {
                    log.info("更新库存失败");
                    throw new BizException("更新库存失败");

                }

            });
        }

        if (!stockSave.isEmpty()) {
            //保存库存
            log.info("批量保存");
            this.saveBatch(stockSave);
        }
        userStockFlowService.saveBatch(stockFlowDOList);

        log.info("新增用户库存完成");


    }


    private StockSaveOrUpdateBO buildStockSaveOrUpdate(List<UserStockMsg> inStorageItemList) {

        List<String> skuCodeList = inStorageItemList.stream().map(UserStockMsg::getSkuCode).collect(Collectors.toList());

        List<StockDO> stockDOlist = this.lambdaQuery().in(StockDO::getSkuCode, skuCodeList).list();

        //key: WarehouseId+SkuCode
        Map<String, StockDO> stockDOMap = stockDOlist.stream()
                .collect(Collectors.toMap(a -> a.getWarehouseId() + a.getSkuCode(), Function.identity()));
        List<StockDO> stockDOSave = new ArrayList<>();
        List<StockDO> stockDOUpdate = new ArrayList<>();

        for (UserStockMsg st : inStorageItemList) {
            String key = st.getWarehouseId() + st.getSkuCode();
            StockDO stockDO = stockDOMap.get(key);
            if (stockDO == null) {
                //新增
                StockDO stock = new StockDO();
                stock.setSkuCode(st.getSkuCode());
                stock.setTotalStock(st.getActualQuantity());
                stock.setLockStock(0);
                stock.setWarehouseId(st.getWarehouseId());
                stock.setGoodsName(st.getGoodsName());
                stock.setUnit(st.getUnit());
                stock.setWarehouseName(st.getWarehouseName());
                stockDOSave.add(stock);
            } else {
                //更新
                Integer totalStock = stockDO.getTotalStock() + st.getActualQuantity();
                StockDO stockUpdateBO = new StockDO();
                stockUpdateBO.setTotalStock(totalStock);
                stockUpdateBO.setId(stockDO.getId());
                stockUpdateBO.setVersion(stockDO.getVersion());
                stockDOUpdate.add(stockUpdateBO);
            }

        }
        return new StockSaveOrUpdateBO(stockDOSave, stockDOUpdate);

    }

    private List<UserStockSyncRecordDO> buildUserStockFlowDO(UserStockAddMsg message) {

        List<UserStockMsg> inStorageItemList = message.getUserStockList();

        List<String> skuCodeList = inStorageItemList.stream().map(UserStockMsg::getSkuCode).collect(Collectors.toList());

        List<StockDO> stockDOlist = this.lambdaQuery().in(StockDO::getSkuCode, skuCodeList).list();

        //key: WarehouseId+SkuCode
        Map<String, StockDO> stockDOMap2 = stockDOlist.stream()
                .collect(Collectors.toMap(a -> a.getWarehouseId() + a.getSkuCode(), Function.identity()));


        List<UserStockSyncRecordDO> stockFlowDOList = BeanCopyUtils.copyBeanList(inStorageItemList, UserStockSyncRecordDO.class);
        Map<String, UserStockMsg> stockDOMap = inStorageItemList.stream()
                .collect(Collectors.toMap(a -> a.getWarehouseId() + a.getSkuCode(), Function.identity()));

        stockFlowDOList.forEach(a -> {

            String key = a.getWarehouseId() + a.getSkuCode();
            UserStockMsg userStockMsg = stockDOMap.get(key);
            StockDO stockDO = stockDOMap2.get(key);
            if (stockDO == null) {

                a.setBeforeQuantity(0);
                a.setAfterQuantity(userStockMsg.getActualQuantity());
            } else {
                a.setBeforeQuantity(stockDO.getTotalStock());
                a.setAfterQuantity(stockDO.getTotalStock() + userStockMsg.getActualQuantity());
            }
            a.setOrderType(0);
            a.setBizNumber(message.getPurchaseInStockOrderId().toString());
            a.setInQuantity(userStockMsg.getActualQuantity());

        });

        return stockFlowDOList;

    }

    @Override
    public Map<String, SkuCodeStockBO> findBySkuCode(List<String> skuCode) {

        List<StockDO> stockDOList = this.lambdaQuery().in(StockDO::getSkuCode, skuCode).list();

        Map<String, SkuCodeStockBO> stockCount = new HashMap<>();

        for (StockDO stockDO : stockDOList) {

            String skuCode1 = stockDO.getSkuCode();
            SkuCodeStockBO skuCodeStockBO = stockCount.get(skuCode1);

            if (skuCodeStockBO == null) {
                SkuCodeStockBO stockBO = new SkuCodeStockBO();
                stockBO.setTotalStock(stockDO.getTotalStock());
                stockCount.put(skuCode1, stockBO);
            } else {
                Integer count2 = skuCodeStockBO.getTotalStock() + stockDO.getTotalStock();
                skuCodeStockBO.setTotalStock(count2);
            }
        }

        return stockCount;
    }

}
