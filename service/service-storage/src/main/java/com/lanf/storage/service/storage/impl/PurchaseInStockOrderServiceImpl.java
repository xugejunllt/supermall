package com.lanf.storage.service.storage.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.common.utils.IStringUtils;
import com.lanf.lock.aop.DistributedLock;
import com.lanf.messagemanager.client.model.dto.SendMqMessageDTO;
import com.lanf.messagemanager.client.service.ISendMqMessageService;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.mybatis.base.PageResult;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.enums.EventCodeEnum;
import com.lanf.rocketmq.model.message.SendSmsMsg;
import com.lanf.rocketmq.model.message.UserStockAddMsg;
import com.lanf.rocketmq.model.message.UserStockMsg;
import com.lanf.security.utils.MerchantIdContext;
import com.lanf.storage.mapper.PurchaseInStockOrderMapper;
import com.lanf.storage.model.bo.StockSaveOrUpdateBO;
import com.lanf.storage.model.bo.StockUpdateBO;
import com.lanf.storage.model.dto.InStockDTO;
import com.lanf.storage.model.dto.InStockItemDTO;
import com.lanf.storage.model.entity.*;
import com.lanf.storage.model.query.PurchaseInStockOrderPageQuery;
import com.lanf.storage.model.vo.PurchaseInStockOrderDetailVO;
import com.lanf.storage.model.vo.PurchaseInStockOrderItemDetailVO;
import com.lanf.storage.model.vo.PurchaseInStockOrderPageVO;
import com.lanf.storage.service.purchase.IPurchaseOrderService;
import com.lanf.storage.service.stock.IStockFlowService;
import com.lanf.storage.service.stock.IStockService;
import com.lanf.storage.service.storage.IInOutStockOrderItemService;
import com.lanf.storage.service.storage.IPurchaseInStockOrderService;
import com.lanf.storage.service.storage.IStorageFlowService;
import com.lanf.storage.service.supplier.ISupplierService;
import com.lanf.storage.service.warehous.IWarehouseService;
import com.lanf.web.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 采购入库单 服务实现类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-05-30
 */
@Slf4j
@Service
public class PurchaseInStockOrderServiceImpl extends ServiceImpl<PurchaseInStockOrderMapper, PurchaseInStockOrderDO> implements IPurchaseInStockOrderService {

    @Autowired
    private IInOutStockOrderItemService storageOrderItemDetailsService;
    @Autowired
    private IWarehouseService warehouseService;
    @Autowired
    private ISupplierService supplierService;
    @Autowired
    private IStorageFlowService storageDetailsService;
    @Autowired
    private IStockFlowService stockFlowService;
    @Autowired
    private IStockService stockService;

    @Lazy
    @Autowired
    private IPurchaseOrderService purchaseOrderService;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private ISendMqMessageService sendMqMessageService;
    
    
    /**
     * 入库
     */
    @Transactional(rollbackFor = {Exception.class})
    @DistributedLock(key = "#inStorageDTO.purchaseInStockOrderId")
    @Override
    public void inStock(InStockDTO inStorageDTO) {


        /**
         * 准备数据
         */
        Long purchaseStorageOrderId = inStorageDTO.getPurchaseInStockOrderId();
        List<InStockItemDTO> inStorageItemList = inStorageDTO.getInStorageItemList();
        PurchaseInStockOrderDO storageOrderDO = this.getById(purchaseStorageOrderId);

        List<InOutStockOrderItemDO> storageOrderItemDetailList = storageOrderItemDetailsService.lambdaQuery().
                eq(InOutStockOrderItemDO::getInOutStockOrderId, inStorageDTO.getPurchaseInStockOrderId()).list();
        Map<Long, InOutStockOrderItemDO> purchaseOrderItemDOMap = storageOrderItemDetailList.stream()
                .collect(Collectors.toMap(InOutStockOrderItemDO::getId, Function.identity()));

        Set<Long> warehouseIdSet = inStorageItemList.stream().map(InStockItemDTO::getWarehouseId).collect(Collectors.toSet());
        List<WarehouseDO> warehouseDOList = warehouseService.lambdaQuery().in(BaseEntity::getId, warehouseIdSet).list();
        Map<Long, WarehouseDO> warehouseDODOMap = warehouseDOList.stream()
                .collect(Collectors.toMap(WarehouseDO::getId, Function.identity()));

        //初始一个同步时间
        inStorageItemList.forEach(a->{

            Date date = new Date();
            a.setSyncTime(date);

        });

        //校验
        inStorageCheck(storageOrderDO, inStorageItemList, inStorageDTO);

        /**
         * 计算DB数据
         */
        //入库总数量
        Integer enterQuantity = 0;
        for (InStockItemDTO is : inStorageItemList) {
            enterQuantity += is.getActualQuantity();
        }
        //构建更新StorageOrderItemDetailsDO
        List<InOutStockOrderItemDO> storageOrderItemDetailsDOUpdate = buildStorageOrderItemDetailsDO(inStorageItemList,
                purchaseOrderItemDOMap);

        //生成库存流水
        List<StockFlowDO> stockFlowList = buildStockFlowDO(inStorageItemList, purchaseOrderItemDOMap, storageOrderDO, warehouseDODOMap);
        //更新入库单实际库存和状态
        PurchaseInStockOrderDO purchaseStorageOrderDOUpdate = buildPurchaseStorageOrderDO(storageOrderDO, enterQuantity);
        //更新商品库存
        StockSaveOrUpdateBO stockSaveOrUpdateBO = buildStockSaveOrUpdate(inStorageItemList, warehouseDODOMap);
        List<StockDO> stockSave = stockSaveOrUpdateBO.getStockSave();
        List<StockUpdateBO> stockUpdate = stockSaveOrUpdateBO.getStockUpdate();

        /**
         * 进行DB操作
         */
        SendMqMessageDTO sendMqMessageDTO = buildSendMqMessageDTO(inStorageDTO,warehouseDODOMap);

        transactionTemplate.execute(status -> {
            try {
                if (!stockUpdate.isEmpty()) {
                    //乐观锁更新 更新商品库存
                    stockUpdate.forEach(a -> {
                        boolean update = stockService.lambdaUpdate().
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
                //更新入库单
                this.updateById(purchaseStorageOrderDOUpdate);
                //更新入库单item数量
                storageOrderItemDetailsService.updateBatchById(storageOrderItemDetailsDOUpdate);
                if (!stockSave.isEmpty()) {
                    //保存库存
                    stockService.saveBatch(stockSave);
                }
                //保存库存流水
                stockFlowService.saveBatch(stockFlowList);
                sendMqMessageService.createSendMqMessage(sendMqMessageDTO);
                // 如果一切正常，事务会自动提交
                return null;
            } catch (Exception e) {
                // 发生异常时手动回滚
                status.setRollbackOnly();
                throw e;

            }
        });
        
        //发送mq 注册事件
        sendMqMessageService.sendMqMessage(sendMqMessageDTO);
    }
    private SendMqMessageDTO buildSendMqMessageDTO(InStockDTO inStorageDTO, Map<Long, WarehouseDO> warehouseDODOMap){

        Long purchaseInStockOrderId = inStorageDTO.getPurchaseInStockOrderId();
        List<InStockItemDTO> inStorageItemList = inStorageDTO.getInStorageItemList();

        UserStockAddMsg messageContent = new UserStockAddMsg();
        List<UserStockMsg> userStockList = BeanCopyUtils.copyBeanList(inStorageItemList,UserStockMsg.class);
        userStockList.forEach(a->{
            WarehouseDO warehouseDO = warehouseDODOMap.get(a.getWarehouseId());
            a.setWarehouseName(warehouseDO.getName());

        });

        String uuid = UUID.randomUUID().toString();
        //KEY purchaseInStockOrderId  + uuid
        String buildBizKey = EventCodeEnum.buildBizKey(purchaseInStockOrderId+":"+uuid, EventCodeEnum.PURCHASE_ORDER_IN_STOCK.getCode());
        messageContent.setBizKeyValue(buildBizKey);
        messageContent.setUserStockList(userStockList);
        messageContent.setTenantId(MerchantIdContext.getMerchantId());
        messageContent.setPurchaseInStockOrderId(purchaseInStockOrderId);
        return new SendMqMessageDTO(TopicName.USER_STOCK_ADD_TOPIC,messageContent);
    }




    private PurchaseInStockOrderDO buildPurchaseStorageOrderDO(PurchaseInStockOrderDO storageOrderDO, Integer enterQuantity) {

        Integer actualStorageQuantity = getActualStorageQuantity(storageOrderDO, enterQuantity);
        Integer status = getInStorageStatus(storageOrderDO, enterQuantity);
        PurchaseInStockOrderDO purchaseStorageOrderDOUpdate = new PurchaseInStockOrderDO();
        purchaseStorageOrderDOUpdate.setId(storageOrderDO.getId());
        purchaseStorageOrderDOUpdate.setStorageStatus(status);
        purchaseStorageOrderDOUpdate.setActualStorageQuantity(actualStorageQuantity);

        return purchaseStorageOrderDOUpdate;
    }

    private Integer getActualStorageQuantity(PurchaseInStockOrderDO storageOrderDO, Integer enterQuantity) {

        return storageOrderDO.getActualStorageQuantity() + enterQuantity;
    }

    /**
     * 获取入库状态
     * 1:部分入库
     * 2:全部入库
     */
    private Integer getInStorageStatus(PurchaseInStockOrderDO storageOrderDO, Integer enterQuantity) {

        Integer actualStorageQuantity = getActualStorageQuantity(storageOrderDO, enterQuantity);
        Integer status = null;
        if (actualStorageQuantity.equals(storageOrderDO.getExpectStorageQuantity())) {
            status = 2;
        } else {
            status = 1;
        }
        return status;
    }

    private void inStorageCheck(PurchaseInStockOrderDO storageOrderDO, List<InStockItemDTO> inStorageItemList, InStockDTO inStorageDTO) {

        if (storageOrderDO == null) {
            throw new BizException("入库单不存在");
        }
        Set<Long> warehouseIdSet = inStorageItemList.stream().map(InStockItemDTO::getWarehouseId).collect(Collectors.toSet());
        int warehouseDOList = warehouseService.lambdaQuery().in(BaseEntity::getId, warehouseIdSet).count();
        if (warehouseIdSet.size() != warehouseDOList) {
            throw new BizException("仓库id不存在");
        }

        Set<Long> purchaseOrderItemIdSet = inStorageItemList.stream().map(InStockItemDTO::getId).collect(Collectors.toSet());
        List<InOutStockOrderItemDO> storageOrderItemDetailList = storageOrderItemDetailsService.lambdaQuery().
                eq(InOutStockOrderItemDO::getInOutStockOrderId, inStorageDTO.getPurchaseInStockOrderId()).list();

        if (purchaseOrderItemIdSet.size() != storageOrderItemDetailList.size()) {
            throw new BizException("入库单部分商品数据不存在");
        }
        Map<Long, InOutStockOrderItemDO> purchaseOrderItemDOMap = storageOrderItemDetailList.stream()
                .collect(Collectors.toMap(InOutStockOrderItemDO::getId, Function.identity()));

        //校验实际入库数量是否合法
        inStorageItemList.forEach(a -> {
            Integer actualQuantity = a.getActualQuantity();
            Integer quantity = purchaseOrderItemDOMap.get(a.getId()).getSurplusQuantity();
            if (actualQuantity > quantity) {
                throw new BizException("入库商品" + a.getId() + "实际入库数量大于最大数量");
            }
        });
    }

    private List<InOutStockOrderItemDO> buildStorageOrderItemDetailsDO(List<InStockItemDTO> inStorageItemList,
                                                                       Map<Long, InOutStockOrderItemDO> purchaseOrderItemDOMap) {

        List<InOutStockOrderItemDO> storageOrderItemDetailsDOUpdate = new ArrayList<>();
        for (InStockItemDTO s : inStorageItemList) {

            InOutStockOrderItemDO storageOrderItemDetailsDO1 = purchaseOrderItemDOMap.get(s.getId());
            InOutStockOrderItemDO storageOrderItemDetailsDO = new InOutStockOrderItemDO();
            storageOrderItemDetailsDO.setId(s.getId());
            //剩余数量
            Integer surplusQuantity = storageOrderItemDetailsDO1.getSurplusQuantity() - s.getActualQuantity();
            storageOrderItemDetailsDO.setSurplusQuantity(surplusQuantity);
            storageOrderItemDetailsDOUpdate.add(storageOrderItemDetailsDO);
        }
        return storageOrderItemDetailsDOUpdate;
    }


    private List<StockFlowDO> buildStockFlowDO(List<InStockItemDTO> inStorageItemList, Map<Long,
            InOutStockOrderItemDO> purchaseOrderItemDOMap, PurchaseInStockOrderDO storageOrderDO, Map<Long, WarehouseDO> warehouseDODOMap) {

        List<StockFlowDO> stockFlowList = new ArrayList<>();
        for (InStockItemDTO is : inStorageItemList) {

            WarehouseDO warehouseDO = warehouseDODOMap.get(is.getWarehouseId());
            InOutStockOrderItemDO storageOrderItemDetailsDO = purchaseOrderItemDOMap.get(is.getId());
            StockFlowDO stockFlowDO = new StockFlowDO();
            stockFlowDO.setOrderType(4);
            stockFlowDO.setSkuCode(storageOrderItemDetailsDO.getSkuCode());
            stockFlowDO.setBizNumber(storageOrderDO.getId().toString());
            stockFlowDO.setWarehouseName(warehouseDO.getName());
            stockFlowDO.setInQuantity(is.getActualQuantity());
            stockFlowDO.setWarehouseId(warehouseDO.getId());
            stockFlowDO.setSyncTime(is.getSyncTime());
            stockFlowList.add(stockFlowDO);
        }
        return stockFlowList;
    }

    private StockSaveOrUpdateBO buildStockSaveOrUpdate(List<InStockItemDTO> inStorageItemList, Map<Long, WarehouseDO> warehouseDODOMap) {

        List<String> skuCodeList = inStorageItemList.stream().map(
                InStockItemDTO::getSkuCode).collect(Collectors.toList());

        List<StockDO> stockDOlist = stockService.lambdaQuery().in(StockDO::getSkuCode, skuCodeList).list();

        //key: WarehouseId+SkuCode
        Map<String, StockDO> stockDOMap = stockDOlist.stream()
                .collect(Collectors.toMap(a -> a.getWarehouseId() + a.getSkuCode(), Function.identity()));


        List<StockDO> stockDOSave = new ArrayList<>();
        List<StockUpdateBO> stockDOUpdate = new ArrayList<>();

        for (InStockItemDTO st : inStorageItemList) {
            String key = st.getWarehouseId() + st.getSkuCode();
            StockDO stockDO = stockDOMap.get(key);
            if (stockDO == null) {
                WarehouseDO warehouseDO = warehouseDODOMap.get(st.getWarehouseId());
                //新增
                StockDO stock = new StockDO();
                stock.setSkuCode(st.getSkuCode());
                stock.setTotalStock(st.getActualQuantity());
                stock.setLockStock(0);
                stock.setWarehouseId(warehouseDO.getId());
                stock.setGoodsName(st.getGoodsName());
                stock.setUnit(st.getUnit());
                stock.setWarehouseName(warehouseDO.getName());
                stockDOSave.add(stock);
            } else {
                //更新
                StockUpdateBO stockUpdateBO = getStockUpdateBO(st, stockDO);
                stockDOUpdate.add(stockUpdateBO);
            }

        }
        return new StockSaveOrUpdateBO(stockDOSave, stockDOUpdate);

    }

    private static StockUpdateBO getStockUpdateBO(InStockItemDTO st, StockDO stockDO) {
        Integer totalStock = stockDO.getTotalStock() + st.getActualQuantity();
        StockUpdateBO stockUpdateBO = new StockUpdateBO();
        stockUpdateBO.setTotalStock(totalStock);
        stockUpdateBO.setId(stockDO.getId());
        stockUpdateBO.setVersion(stockDO.getVersion());

        return stockUpdateBO;
    }

    @Override
    public PageResult<PurchaseInStockOrderPageVO> purchaseInStockOrderPage(PurchaseInStockOrderPageQuery query) {

        IPage<PurchaseInStockOrderDO> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<PurchaseInStockOrderDO> purchaseStorageOrderPage = this.lambdaQuery().
                eq(query.getInStockStatus() != null, PurchaseInStockOrderDO::getStorageStatus, query.getInStockStatus()).
                orderByDesc(BaseEntity::getUpdateTime)
                .page(page);

        if (purchaseStorageOrderPage.getRecords().isEmpty()) {

            return PageResult.emptyResult(PurchaseInStockOrderPageVO.class);
        }

        PageResult<PurchaseInStockOrderPageVO> pageResult = PageResult.toPageResult(page, PurchaseInStockOrderPageVO.class);

        List<PurchaseInStockOrderDO> records = purchaseStorageOrderPage.getRecords();


        return pageResult;
    }

    @Override
    public PurchaseInStockOrderDetailVO purchaseInStockOrderDetail(Long id) {

        PurchaseInStockOrderDO storageOrderDO = this.getById(id);
        if (storageOrderDO == null) {
            throw new BizException("采购入库单不存在");
        }
        List<InOutStockOrderItemDO> storageOrderItemDetailsList = storageOrderItemDetailsService.
                lambdaQuery().eq(InOutStockOrderItemDO::getInOutStockOrderId, id).list();
        if (storageOrderItemDetailsList.isEmpty()) {
            throw new BizException("采购入库单商品不存在");
        }

        Integer totalExpectStorageQuantity = storageOrderDO.getExpectStorageQuantity();
        Integer totalActualStorageQuantity = storageOrderDO.getActualStorageQuantity();
        Integer totalActualSurplusQuantity = totalExpectStorageQuantity - totalActualStorageQuantity;
        List<PurchaseInStockOrderItemDetailVO> purchaseStorageOrderItemDetailVOList =
                BeanCopyUtils.copyBeanList(storageOrderItemDetailsList, PurchaseInStockOrderItemDetailVO.class);
        purchaseStorageOrderItemDetailVOList.forEach(a -> {
            //实际入库数量 = 总数量-剩余数量
            a.setActualQuantity(a.getTotalQuantity() - a.getSurplusQuantity());
        });
        PurchaseInStockOrderDetailVO purchaseStorageOrderDetailVO = new PurchaseInStockOrderDetailVO();
        purchaseStorageOrderDetailVO.setTotalExpectStorageQuantity(totalExpectStorageQuantity);
        purchaseStorageOrderDetailVO.setTotalActualStorageQuantity(totalActualStorageQuantity);
        purchaseStorageOrderDetailVO.setTotalActualSurplusQuantity(totalActualSurplusQuantity);
        purchaseStorageOrderDetailVO.setCode(storageOrderDO.getCode());
        purchaseStorageOrderDetailVO.setPurchaseStorageOrderItemDetailVOList(purchaseStorageOrderItemDetailVOList);
        purchaseStorageOrderDetailVO.setId(storageOrderDO.getId());
        return purchaseStorageOrderDetailVO;
    }

}
