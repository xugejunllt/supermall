package com.lanf.goods.service.goods.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.common.utils.IdUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.goods.mapper.StockMapper;
import com.lanf.goods.model.bo.SkuCodeStockBO;
import com.lanf.goods.model.bo.StockSaveOrUpdateBO;
import com.lanf.goods.model.dto.DeductStockDTO;
import com.lanf.goods.model.dto.StockEnoughDTO;
import com.lanf.goods.model.entity.GoodsSkuDO;
import com.lanf.goods.model.entity.StockDO;
import com.lanf.goods.model.entity.UserStockFlowDO;
import com.lanf.goods.model.entity.UserStockSyncRecordDO;
import com.lanf.goods.model.vo.DeductStockVO;
import com.lanf.goods.model.vo.StockEnoughVO;
import com.lanf.goods.service.goods.IGoodsSkuService;
import com.lanf.goods.service.goods.IStockService;
import com.lanf.goods.service.goods.IUserStockFlowService;
import com.lanf.goods.service.goods.IUserStockSyncRecordService;
import com.lanf.goods.utils.ProductServiceUtils;
import com.lanf.rocketmq.model.message.UserStockAddMsg;
import com.lanf.rocketmq.model.message.UserStockMsg;
import com.lanf.tcc.service.ITccOperationService;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hmily.annotation.HmilyTCC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private IUserStockSyncRecordService userStockSyncRecordService;

    @Autowired
    private IUserStockFlowService userStockFlowService;
    @Autowired
    private ITccOperationService tccOperationService;
    @Autowired
    private IGoodsSkuService goodsSkuService;

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void addUserStock(UserStockAddMsg message) {
        log.info("新增用户库存");
        /**
         * 预生成userStockId
         */
        List<UserStockMsg> userStockList = message.getUserStockList();
        userStockList.forEach(a -> {
            long generated = IdUtils.generateId();
            a.setUserStockId(generated);
        });
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
                        set(StockDO::getUsableStock, a.getUsableStock()).
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
        userStockSyncRecordService.saveBatch(stockFlowDOList);

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
                stock.setUsableStock(st.getActualQuantity());
                stock.setLockStock(0);
                stock.setWarehouseId(st.getWarehouseId());
                stock.setGoodsName(st.getGoodsName());
                stock.setUnit(st.getUnit());
                stock.setWarehouseName(st.getWarehouseName());
                stock.setId(st.getUserStockId());
                stockDOSave.add(stock);
            } else {
                //更新
                Integer totalStock = stockDO.getUsableStock() + st.getActualQuantity();
                StockDO stockUpdateBO = new StockDO();
                stockUpdateBO.setUsableStock(totalStock);
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
                a.setUserStockId(userStockMsg.getUserStockId());
                a.setBeforeQuantity(0);
                a.setAfterQuantity(userStockMsg.getActualQuantity());
            } else {
                a.setUserStockId(stockDO.getId());
                a.setBeforeQuantity(stockDO.getUsableStock());
                a.setAfterQuantity(stockDO.getUsableStock() + userStockMsg.getActualQuantity());
            }
            a.setOrderType(0);
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
                stockBO.setTotalStock(stockDO.getUsableStock());
                stockCount.put(skuCode1, stockBO);
            } else {
                Integer count2 = skuCodeStockBO.getTotalStock() + stockDO.getUsableStock();
                skuCodeStockBO.setTotalStock(count2);
            }
        }

        return stockCount;
    }
    @Transactional
    @HmilyTCC(confirmMethod = "confirmDeductStock", cancelMethod = "cancelDeductStock")
    @Override
    public DeductStockVO deductStock(DeductStockDTO deductStockDTO) {
        log.info("扣减库存开始");


        String skuCode = deductStockDTO.getSkuCode();

        List<StockDO> stockDOList = this.lambdaQuery().eq(StockDO::getSkuCode, skuCode).list();
        if (stockDOList.isEmpty()) {
            log.info("库存不存在");
            throw new BizException("库存不存在");
        }

        /**
         *
         * 可能多个仓库 skucode 暂时取其中一个
         *
         */
        StockDO stockDO = findStockDO(skuCode);
        Integer totalStock = stockDO.getUsableStock();
        if (totalStock < deductStockDTO.getQuantity()) {
            log.info("库存不足");
            throw new BizException("库存不足");
        }

        Long updateVersion = stockDO.getVersion()+1;
        //扣减后的剩余总库存
        Integer  updateTotalStock = totalStock - deductStockDTO.getQuantity();
        //冻结库存
        Integer updateLockStock = stockDO.getLockStock() + deductStockDTO.getQuantity();

        String bizKey = generateDeductStockBizKey(deductStockDTO.getOrderId(),stockDO.getId(),0);
        /**
         * DB操作
         */
        tccOperationService.tryOperation(bizKey);
        boolean update = this.lambdaUpdate().
                eq(StockDO::getId, stockDO.getId()).
                eq(StockDO::getVersion, stockDO.getVersion()).
                set(StockDO::getUsableStock, updateTotalStock).
                set(StockDO::getLockStock, updateLockStock).
                set(StockDO::getVersion, updateVersion).
                update();
        if (!update) {
            log.info("扣减库存失败");
            throw new BizException("扣减库存失败");
        }
        return buildDeductStockVO( skuCode, deductStockDTO.getQuantity());
    }


    private DeductStockVO buildDeductStockVO(String skuCode,Integer  quantity) {

        GoodsSkuDO goodsSkuDO = goodsSkuService.lambdaQuery().eq(GoodsSkuDO::getSkuCode, skuCode)
                .one();
        //订单总金额
        BigDecimal totalAmount =  ProductServiceUtils.calculateTotalAmount(goodsSkuDO.getPrice(), quantity);
        DeductStockVO deductStockVO = new DeductStockVO();
        deductStockVO.setTotalAmount(totalAmount);

        return deductStockVO;
    }

    @Transactional
    public void confirmDeductStock(DeductStockDTO deductStockDTO) {

        log.info("confirmDeductStock[{}]", deductStockDTO);

        String skuCode = deductStockDTO.getSkuCode();
        StockDO stockDO = findStockDO(skuCode);
        Long updateVersion = stockDO.getVersion()+1;
        //扣减冻结库存
        Integer lockStock = stockDO.getLockStock() - deductStockDTO.getQuantity();
        UserStockFlowDO userStockFlowDO = buildUserStockFlowDO(deductStockDTO, stockDO);
        String bizKey = generateDeductStockBizKey(deductStockDTO.getOrderId(), stockDO.getId(), 0);
        boolean operation = tccOperationService.confirmOperation(bizKey);
        if ( !operation){
            log.info("confirm已执行");
            return;
        }
        userStockFlowService.save(userStockFlowDO);
        boolean update = this.lambdaUpdate().
                eq(StockDO::getId, stockDO.getId()).
                eq(StockDO::getVersion, stockDO.getVersion()).
                set(StockDO::getLockStock, lockStock).
                set(StockDO::getVersion, updateVersion).
                update();
        if (!update) {
            log.info("解冻失败");
            throw new BizException("解冻失败");
        }
    }

    /**
     *
     * 生成库存流水业务key
     *
     *
     */
    private String generateDeductStockBizKey(Long orderId,Long userStockId,Integer eventType) {

        return orderId+":"+userStockId+":"+eventType;
    }
    private UserStockFlowDO buildUserStockFlowDO(DeductStockDTO deductStockDTO,StockDO stockDO) {

        //总库存 = 可使用+已冻结
        Integer totalStock = stockDO.getUsableStock()+ deductStockDTO.getQuantity();
        Integer eventType = 0;
        UserStockFlowDO userStockFlowDO = new UserStockFlowDO();
        userStockFlowDO.setUserStockId(stockDO.getId());
        userStockFlowDO.setOrderId(deductStockDTO.getOrderId());
        userStockFlowDO.setEventType(eventType);
        // 总库存 = 可使用+已冻结
        userStockFlowDO.setBeforeQuantity(totalStock);
        userStockFlowDO.setAfterQuantity(totalStock - deductStockDTO.getQuantity());
        userStockFlowDO.setOutQuantity(deductStockDTO.getQuantity());
        return userStockFlowDO;
    }
    @Transactional
    public void cancelDeductStock(DeductStockDTO deductStockDTO) {

        log.info("cancelDeductStock[{}]", deductStockDTO);
        String skuCode = deductStockDTO.getSkuCode();
        StockDO stockDO = findStockDO(skuCode);
        Integer usableStock = stockDO.getUsableStock();
        Long updateVersion = stockDO.getVersion()+1;
        //扣减后的剩余总库存
        Integer  updateUsableStock = usableStock + deductStockDTO.getQuantity();
        //冻结库存
        Integer updateLockStock = stockDO.getLockStock() - deductStockDTO.getQuantity();
        String bizKey = generateDeductStockBizKey(deductStockDTO.getOrderId(),stockDO.getId(),0);
        /**
         * DB操作
         */
        boolean operation = tccOperationService.cancelOperation(bizKey);
        if ( !operation){

            return;
        }
        boolean update = this.lambdaUpdate().
                eq(StockDO::getId, stockDO.getId()).
                eq(StockDO::getVersion, stockDO.getVersion()).
                set(StockDO::getUsableStock, updateUsableStock).
                set(StockDO::getLockStock, updateLockStock).
                set(StockDO::getVersion, updateVersion).
                update();
        if (!update) {
            log.info("cancelDeductStock失败");
            throw new BizException("cancelDeductStock失败");
        }


    }

    private StockDO findStockDO(String  skuCode){
        List<StockDO> stockDOList = this.lambdaQuery().eq(StockDO::getSkuCode, skuCode).list();
        if (stockDOList.isEmpty()) {
            log.info("库存不存在");
            throw new BizException("库存不存在");
        }
        /**
         *
         * 可能多个仓库 skucode 暂时取其中一个
         *
         */

        return stockDOList.get(0);
    }
    @Override
    public StockEnoughVO isStockEnough(StockEnoughDTO dto) {

        StockDO stockDO = findStockDO(dto.getSkuCode());
        Boolean enough = true;
        if (stockDO.getUsableStock() < dto.getQuantity()) {
            log.info("库存不足");
            enough = false;
        }
        StockEnoughVO stockEnoughVO = new StockEnoughVO();
        stockEnoughVO.setSkuId(stockDO.getId());
        stockEnoughVO.setEnough(enough);

        return stockEnoughVO;
    }
}
