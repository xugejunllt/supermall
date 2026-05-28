package com.lanf.storage.service.storage.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.aftersales.mq.message.SalesInStockOrderAddMessage;
import com.lanf.aftersales.mq.message.SalesInStockOrderItemAdd;
import com.lanf.api.storage.model.dto.OutStockItemDTO;
import com.lanf.api.storage.model.dto.OutStockSalesOutStockOrderDTO;
import com.lanf.api.storage.model.enums.StorageStatusEnum;
import com.lanf.api.storage.model.query.SalesOutStockOrderPageQuery;
import com.lanf.api.storage.model.vo.PurchaseInStockOrderItemDetailVO;
import com.lanf.api.storage.model.vo.SalesOutStockOrderDetailVO;
import com.lanf.api.storage.model.vo.SalesOutStockOrderPageVO;
import com.lanf.api.storage.mq.constant.StorageClientTopicName;
import com.lanf.api.storage.mq.message.SalesOutStockOrderFinishMessage;
import com.lanf.common.utils.*;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.model.enums.FlowNoPrefixEnum;
import com.lanf.constant.model.enums.storage.StockFlowTypeEnum;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.utils.IdUtils;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.rocketmq.util.RocketMqClient;
import com.lanf.storage.mapper.SalesOutStockOrderMapper;
import com.lanf.storage.model.bo.StockUpdateBO;
import com.lanf.storage.model.entity.*;
import com.lanf.storage.service.stock.IStockFlowService;
import com.lanf.storage.service.stock.IStockService;
import com.lanf.storage.service.storage.IInOutStockOrderItemService;
import com.lanf.storage.service.storage.ISalesOutStockOrderService;
import com.lanf.storage.service.warehous.IWarehouseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 销售出库单 服务实现类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-09
 */
@Slf4j
@Service
public class SalesOutStockOrderServiceImpl extends ServiceImpl<SalesOutStockOrderMapper, SalesOutStockOrderDO> implements ISalesOutStockOrderService {


    @Autowired
    private IWarehouseService warehouseService;
    @Autowired
    private IInOutStockOrderItemService iInOutStockOrderItemService;
    @Autowired
    private IStockService stockService;

    @Autowired
    private IStockFlowService stockFlowService;
    @Autowired
    private IInOutStockOrderItemService storageOrderItemDetailsService;

    @Autowired
    private RocketMqClient rocketMqClient;



    @Transactional
    @Override
    public void salesStockOrderAdd(SalesInStockOrderAddMessage message) {


        List<SalesOutStockOrderDO> salesOutStockOrderDOList = new ArrayList<>();
        List<InOutStockOrderItemDO> inOutStockOrderItemDOList = new ArrayList<>();

        SalesOutStockOrderDO stockOrderDO = this.lambdaQuery()
                .eq(SalesOutStockOrderDO::getOrderId, message.getAfterSalesOrderId())
                .one();
        if (stockOrderDO != null) {
            throw new BizException("换货退货入库单已存在");
        }

        Long id = IdUtils.generateId();
        SalesOutStockOrderDO salesOutStockOrderDO = new SalesOutStockOrderDO();
        salesOutStockOrderDO.setId(id);
        salesOutStockOrderDO.setCode(CodeGenerateUtils.generaCode());
        salesOutStockOrderDO.setOrderId(message.getAfterSalesOrderId());
        // salesOutStockOrderDO.setExpectQuantity(message.getTotalQuantity());
        salesOutStockOrderDO.setStorageStatus(StorageStatusEnum.WAIT_OUTBOUND);
        // salesOutStockOrderDO.setShopId(message.getShopId());
        //  salesOutStockOrderDO.setWarehouseId(shopVO.getBusinessId());
        salesOutStockOrderDOList.add(salesOutStockOrderDO);
        //
        List<SalesInStockOrderItemAdd> inOutStockOrderItemDTOList = message.getSalesInStockOrderItemAddDTOList();
        inOutStockOrderItemDTOList.forEach(b -> {
            //
            InOutStockOrderItemDO inOutStockOrderItemDO = new InOutStockOrderItemDO();
            inOutStockOrderItemDO.setGoodsName(b.getGoodsName());
            inOutStockOrderItemDO.setSkuCode(b.getSkuCode());
            inOutStockOrderItemDO.setTotalQuantity(b.getQuantity());
            inOutStockOrderItemDO.setSurplusQuantity(b.getQuantity());
            inOutStockOrderItemDO.setUnit(b.getSkuName());
            inOutStockOrderItemDO.setInOutStockOrderId(id);
            inOutStockOrderItemDOList.add(inOutStockOrderItemDO);
        });

        //进行保存
        this.saveBatch(salesOutStockOrderDOList);
        iInOutStockOrderItemService.saveBatch(inOutStockOrderItemDOList);

    }


    private Long getWarehouseId() {
        //获取仓库id

        WarehouseDO one = warehouseService.lambdaQuery().one();

        return one.getId();

    }





    @Transactional
    @Override
    public void outStockSalesOutStockOrder(OutStockSalesOutStockOrderDTO dto) {

        Long salesOutStockOrderId = dto.getSalesOutStockOrderId();

        SalesOutStockOrderDO salesOutStockOrderDO = this.getById(salesOutStockOrderId);
        if (salesOutStockOrderDO == null) {
            log.error("出库单不存在: id={}", salesOutStockOrderId);
            throw new BizException("出库单不存在");
        }
        if (StorageStatusEnum.COMPLETED.equals(salesOutStockOrderDO.getStorageStatus())){

            log.warn("出库单已出库");
            throw new BizException("出库单已出库");
        }

        if (!StorageStatusEnum.WAIT_OUTBOUND.equals(salesOutStockOrderDO.getStorageStatus())){
            log.warn("出库单非待出库状态");
            throw new BizException("出库单非待出库状态");
        }

        List<InOutStockOrderItemDO> list = storageOrderItemDetailsService.lambdaQuery()
                .eq(InOutStockOrderItemDO::getInOutStockOrderId, salesOutStockOrderId)
                .list();
        List<String> skuCodeList = list.stream().map(InOutStockOrderItemDO::getSkuCode)
                .collect(Collectors.toList());

        List<StockDO> stockDOList = stockService.lambdaQuery().in(StockDO::getSkuCode, skuCodeList)
                .list();
        Map<String, StockDO> stockMap = stockDOList.stream()
                .collect(Collectors.toMap(
                        s -> s.getSkuCode() + ":" + s.getWarehouseId(),
                        s -> s,
                        (existing, replacement) -> existing
                ));

        for (InOutStockOrderItemDO itemDO : list){
            StockDO stockDO = stockMap.get(itemDO.getSkuCode() + ":" + itemDO.getWarehouseId());
            if (stockDO.getPreStock()< itemDO.getTotalQuantity()){
                throw new BizException("库存不足");
            }
        }

        List<StockDO> updateStockDO = new ArrayList<>();
        List<StockFlowDO> flowDOList = new ArrayList<>();
        List<InOutStockOrderItemDO> updatedItems = new ArrayList<>();

        for (InOutStockOrderItemDO itemDO : list){
            StockDO stockDO = stockMap.get(itemDO.getSkuCode() + ":" + itemDO.getWarehouseId());
            Integer beforeQuantity = stockDO.getPreStock()+stockDO.getUsableStock();
            Integer changeQuantity = itemDO.getTotalQuantity();
            Integer afterQuantity = beforeQuantity - itemDO.getTotalQuantity();

            Integer updatePreStock = stockDO.getPreStock() - itemDO.getTotalQuantity();
            stockDO.setPreStock(updatePreStock);
            updateStockDO.add(stockDO);
            //
            itemDO.setSurplusQuantity(0);
            updatedItems.add(itemDO);
            //
            StockFlowDO stockFlowDO = new StockFlowDO();
            stockFlowDO.setFlowNo(CodeGenerateUtils.generateFlowNo(FlowNoPrefixEnum.STOCK_FLOW));
            stockFlowDO.setStockId(stockDO.getId());
            stockFlowDO.setFlowType(StockFlowTypeEnum.SALES_OUTBOUND);
            stockFlowDO.setSkuCode(stockDO.getSkuCode());
            stockFlowDO.setBizOrderId(salesOutStockOrderDO.getId());
            stockFlowDO.setOrderId(salesOutStockOrderDO.getOrderId());
            stockFlowDO.setBeforeQuantity(beforeQuantity);
            stockFlowDO.setChangeQuantity(changeQuantity);
            stockFlowDO.setAfterQuantity(afterQuantity);
            stockFlowDO.setWarehouseId(stockDO.getWarehouseId());
            stockFlowDO.setWarehouseName(stockDO.getWarehouseName());
            stockFlowDO.setTenantId(stockDO.getTenantId());
            stockFlowDO.setCreateDate(DateUtils.format(new Date(), DateUtils.DATE));
            flowDOList.add(stockFlowDO);

        }
        boolean update1 = this.lambdaUpdate()
                .eq(BaseEntity::getId, salesOutStockOrderId)
                .eq(SalesOutStockOrderDO::getVersion, salesOutStockOrderDO.getVersion())
                .set(SalesOutStockOrderDO::getStorageStatus, StorageStatusEnum.COMPLETED)
                .set(SalesOutStockOrderDO::getVersion, salesOutStockOrderDO.getVersion() + 1)
                .update();
        if (!update1) {
            log.warn("更新销售出库单失败");
            throw new BizException("更新销售出库单失败");
        }
        for (StockDO stockDO : updateStockDO){
            boolean update = stockService.lambdaUpdate()
                    .eq(BaseEntity::getId, stockDO.getId())
                    .eq(StockDO::getVersion, stockDO.getVersion())
                    .set(StockDO::getVersion, stockDO.getVersion() + 1)
                    .set(StockDO::getPreStock, stockDO.getPreStock())
                    .update();
            if (!update) {
                log.warn("销售出库单更新库存失败");
                throw new BizException("销售出库单更新库存失败");
            }
        }
        for (InOutStockOrderItemDO itemDO : updatedItems){
            boolean update = storageOrderItemDetailsService.lambdaUpdate()
                    .eq(BaseEntity::getId, itemDO.getId())
                    .set(InOutStockOrderItemDO::getSurplusQuantity, itemDO.getSurplusQuantity())
                    .update();
            if (!update) {
                log.warn("更新销售出库单商品项目失败");
                throw new BizException("更新销售出库单商品项目失败");
            }
        }
        stockFlowService.saveBatch(flowDOList);
        sendOutStockFinishMessage(salesOutStockOrderDO);
    }

    /**
     * 检查并更新出库单状态
     *
     * @param salesOutStockOrderDO 出库单
     * @param updatedItems 更新后的明细列表
     * @return 新的出库状态
     */
    private StorageStatusEnum checkAndUpdateOutStockStatus(SalesOutStockOrderDO salesOutStockOrderDO, 
                                                            List<InOutStockOrderItemDO> updatedItems) {
        // 检查是否所有商品都已出库完毕
        boolean allOutStocked = updatedItems.stream()
                .allMatch(item -> item.getSurplusQuantity() <= 0);
        
        if (allOutStocked) {
            log.info("出库单 {} 全部出库完成", salesOutStockOrderDO.getId());
            return StorageStatusEnum.COMPLETED; // 全部出库
        } else {
            log.info("出库单 {} 部分出库", salesOutStockOrderDO.getId());
            return StorageStatusEnum.PARTIAL_OUTBOUND; // 部分出库
        }
    }

    /**
     * 扣减库存（根据 skuCode + warehouseId 查询）
     *
     * @param outStockItemList 出库商品列表
     * @param warehouseId 仓库ID
     * @return 库存更新列表
     */
    private List<StockUpdateBO> deductStock(List<OutStockItemDTO> outStockItemList, Long warehouseId) {
        List<String> skuCodeList = outStockItemList.stream()
                .map(OutStockItemDTO::getSkuCode)
                .collect(Collectors.toList());
        
        // 根据 skuCode 和 warehouseId 查询库存
        ThreadLocalUtils.addIgnoreTableName(true);
        List<StockDO> stockDOList = stockService.lambdaQuery()
                .in(StockDO::getSkuCode, skuCodeList)
                .eq(StockDO::getWarehouseId, warehouseId)
                .list();
        
        if (stockDOList.size() != skuCodeList.size()) {
            throw new BizException("部分商品库存记录不存在");
        }
        
        Map<String, StockDO> stockDOMap = stockDOList.stream()
                .collect(Collectors.toMap(StockDO::getSkuCode, Function.identity()));
        
        List<StockUpdateBO> stockUpdateList = new ArrayList<>();
        
        // 检查库存是否充足并构建更新对象
        for (OutStockItemDTO item : outStockItemList) {
            String skuCode = item.getSkuCode();
            StockDO stockDO = stockDOMap.get(skuCode);

            if (stockDO == null) {
                throw new BizException("商品 " + skuCode + " 在仓库中不存在");
            }

            // 检查预售库存是否充足
            Integer preStock = stockDO.getPreStock();
            if (item.getActualQuantity() > preStock) {
                throw new BizException("商品 " + skuCode + " 库存不足，当前库存: " + preStock);
            }

            // 扣减预售库存
            StockUpdateBO updateBO = new StockUpdateBO();
            updateBO.setId(stockDO.getId());
            updateBO.setUsableStock(preStock - item.getActualQuantity());
            updateBO.setVersion(stockDO.getVersion());
            stockUpdateList.add(updateBO);
        }
        
        return stockUpdateList;
    }

    /**
     * 批量执行数据库更新
     */
    private void executeBatchUpdate(Long salesOutStockOrderId, 
                                   SalesOutStockOrderDO originalOrder,
                                   List<InOutStockOrderItemDO> updatedItems,
                                   List<StockUpdateBO> stockUpdateList,
                                   List<StockFlowDO> stockFlowList,
                                   StorageStatusEnum newStatus) {
        // 1. 更新出库单状态和版本号
        boolean updateOrder = this.lambdaUpdate()
                .eq(BaseEntity::getId, salesOutStockOrderId)
                .eq(SalesOutStockOrderDO::getVersion, originalOrder.getVersion())
                .set(SalesOutStockOrderDO::getStorageStatus, newStatus)
                .set(SalesOutStockOrderDO::getVersion, originalOrder.getVersion() + 1)
                .update();
        
        if (!updateOrder) {
            throw new BizException("更新出库单失败，可能存在并发冲突");
        }
        
        // 2. 更新出库单明细
        iInOutStockOrderItemService.updateBatchById(updatedItems);
        
        // 3. 更新库存（使用乐观锁）
        stockUpdateList.forEach(updateBO -> {
            boolean update = stockService.lambdaUpdate()
                    .eq(StockDO::getId, updateBO.getId())
                    .eq(StockDO::getVersion, updateBO.getVersion())
                    .set(StockDO::getPreStock, updateBO.getUsableStock())
                    .set(StockDO::getVersion, updateBO.getVersion() + 1)
                    .update();
            
            if (!update) {
                throw new BizException("更新库存失败，可能存在并发冲突");
            }
        });
        
        // 4. 保存库存流水
        stockFlowService.saveBatch(stockFlowList);
    }

    /**
     * 发送出库完成消息
     */
    private void sendOutStockFinishMessage(SalesOutStockOrderDO salesOutStockOrderDO) {

        
        SalesOutStockOrderFinishMessage message = new SalesOutStockOrderFinishMessage();
        message.setOrderId(salesOutStockOrderDO.getOrderId());
        message.setUserId(salesOutStockOrderDO.getUserId());
        rocketMqClient.sendMessage(
                StorageClientTopicName.SALES_OUT_STOCK_ORDER_FINISH_TOPIC, 
                JsonUtils.toJsonString(message));
        
    }

    /**
     * 构建库存流水
     */
    private List<StockFlowDO> buildStockFlowDO(List<OutStockItemDTO> inStorageItemList, 
                                               Map<Long, InOutStockOrderItemDO> purchaseOrderItemDOMap, 
                                               SalesOutStockOrderDO salesOutStockOrderDO, 
                                               WarehouseDO warehouseDO,
                                               List<StockUpdateBO> stockUpdateList) {
        List<StockFlowDO> stockFlowList = new ArrayList<>();
        
        // 构建 stockId 映射
        Map<String, Long> skuToStockIdMap = stockUpdateList.stream()
                .collect(Collectors.toMap(
                        bo -> inStorageItemList.stream()
                                .filter(item -> {
                                    StockDO stock = stockService.getById(bo.getId());
                                    return stock != null && stock.getSkuCode().equals(item.getSkuCode());
                                })
                                .findFirst()
                                .map(OutStockItemDTO::getSkuCode)
                                .orElse(""),
                        StockUpdateBO::getId
                ));
        
        for (OutStockItemDTO item : inStorageItemList) {
            InOutStockOrderItemDO orderItem = purchaseOrderItemDOMap.get(item.getOutStockItemId());
            if (orderItem == null) {
                continue;
            }
            
            StockFlowDO stockFlowDO = new StockFlowDO();
            stockFlowDO.setFlowNo(CodeGenerateUtils.generateFlowNo(com.lanf.constant.model.enums.FlowNoPrefixEnum.STOCK_FLOW));
            stockFlowDO.setBizOrderId(orderItem.getInOutStockOrderId());
            stockFlowDO.setOrderId(salesOutStockOrderDO.getOrderId());
            stockFlowDO.setSkuCode(item.getSkuCode());
            
            // 设置库存ID
            Long stockId = skuToStockIdMap.get(item.getSkuCode());
            if (stockId != null) {
                stockFlowDO.setStockId(stockId);
            }
            
            // 设置仓库信息
            stockFlowDO.setWarehouseId(warehouseDO.getId());
            stockFlowDO.setWarehouseName(warehouseDO.getName());
            
            // 设置流水类型：出库
            stockFlowDO.setFlowType(StockFlowTypeEnum.SALES_OUTBOUND);
            
            // 计算变更前、变动、变更后数量
            StockDO stock = stockService.getById(stockId);
            if (stock != null) {
                Integer beforeQuantity = stock.getPreStock() + item.getActualQuantity();
                stockFlowDO.setBeforeQuantity(beforeQuantity);
                stockFlowDO.setChangeQuantity(-item.getActualQuantity()); // 负数表示减少
                stockFlowDO.setAfterQuantity(beforeQuantity - item.getActualQuantity());
            }
            
            stockFlowDO.setTenantId(salesOutStockOrderDO.getTenantId());
            stockFlowDO.setCreateDate(java.time.LocalDate.now().toString());
            
            stockFlowList.add(stockFlowDO);
        }
        
        return stockFlowList;
    }

    private void outStockCheck(SalesOutStockOrderDO salesOutStockOrderDO, List<OutStockItemDTO> inStorageItemList, Long salesOutStockOrderId) {




    }

    private List<InOutStockOrderItemDO> buildStorageOrderItemDetailsDO(List<OutStockItemDTO> inStorageItemList,
                                                                       Map<Long, InOutStockOrderItemDO> purchaseOrderItemDOMap) {

        List<InOutStockOrderItemDO> storageOrderItemDetailsDOUpdate = new ArrayList<>();
        for (OutStockItemDTO s : inStorageItemList) {

            InOutStockOrderItemDO storageOrderItemDetailsDO1 = purchaseOrderItemDOMap.get(s.getOutStockItemId());
            InOutStockOrderItemDO storageOrderItemDetailsDO = new InOutStockOrderItemDO();
            storageOrderItemDetailsDO.setId(s.getOutStockItemId());
            //剩余数量
            Integer surplusQuantity = storageOrderItemDetailsDO1.getSurplusQuantity() - s.getActualQuantity();
            storageOrderItemDetailsDO.setSurplusQuantity(surplusQuantity);
            storageOrderItemDetailsDOUpdate.add(storageOrderItemDetailsDO);
        }
        return storageOrderItemDetailsDOUpdate;
    }





    @Override
    public PageResult<SalesOutStockOrderPageVO> salesOutStockOrderPageQuery(SalesOutStockOrderPageQuery query) {

        IPage<SalesOutStockOrderDO> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<SalesOutStockOrderDO> purchaseStorageOrderPage = this.lambdaQuery().
                eq(query.getInStockStatus() != null, SalesOutStockOrderDO::getStorageStatus, query.getInStockStatus()).
                eq(!ObjectUtils.isEmpty(query.getOrderId()), SalesOutStockOrderDO::getOrderId, query.getOrderId()).
                orderByDesc(BaseEntity::getUpdateTime)
                .page(page);

        if (purchaseStorageOrderPage.getRecords().isEmpty()){

            return PageResult.emptyResult();
        }

        PageResult<SalesOutStockOrderPageVO> result = new PageResult<>();
        result.setTotal(purchaseStorageOrderPage.getTotal());
        result.setSize(purchaseStorageOrderPage.getSize());
        result.setRecords(BeanCopyUtils.copyBeanList(purchaseStorageOrderPage.getRecords(), SalesOutStockOrderPageVO.class));

        return result;
    }

    @Override
    public SalesOutStockOrderDetailVO salesOutStockOrderDetailQuery(Long id) {

        SalesOutStockOrderDO storageOrderDO = this.getById(id);
        if (storageOrderDO == null) {
            throw new BizException("销售出库单不存在");
        }
        List<InOutStockOrderItemDO> storageOrderItemDetailsList = storageOrderItemDetailsService.
                lambdaQuery().eq(InOutStockOrderItemDO::getInOutStockOrderId, id).list();
        if (storageOrderItemDetailsList.isEmpty()) {
            throw new BizException("销售出库单商品不存在");
        }

        List<PurchaseInStockOrderItemDetailVO> purchaseStorageOrderItemDetailVOList =
                BeanCopyUtils.copyBeanList(storageOrderItemDetailsList, PurchaseInStockOrderItemDetailVO.class);
        purchaseStorageOrderItemDetailVOList.forEach(a -> {
            //实际入库数量 = 总数量-剩余数量
            a.setActualQuantity(a.getTotalQuantity() - a.getSurplusQuantity());
        });
        SalesOutStockOrderDetailVO purchaseStorageOrderDetailVO = new SalesOutStockOrderDetailVO();
        BeanCopyUtils.copy(storageOrderDO, purchaseStorageOrderDetailVO);
        purchaseStorageOrderDetailVO.setPurchaseStorageOrderItemDetailVOList(purchaseStorageOrderItemDetailVOList);
        return purchaseStorageOrderDetailVO;
    }
}
