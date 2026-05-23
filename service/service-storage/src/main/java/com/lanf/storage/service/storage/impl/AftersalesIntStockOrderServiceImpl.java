package com.lanf.storage.service.storage.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.aftersales.mq.message.SalesInStockOrderAddMessage;
import com.lanf.aftersales.mq.message.SalesInStockOrderItemAdd;
import com.lanf.api.storage.model.enums.StorageStatusEnum;
import com.lanf.common.utils.CodeGenerateUtils;
import com.lanf.common.utils.DateUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.model.enums.FlowNoPrefixEnum;
import com.lanf.constant.model.enums.storage.StockFlowTypeEnum;
import com.lanf.constant.utils.IdUtils;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.rocketmq.util.RocketMqClient;
import com.lanf.storage.mapper.AftersalesIntStockOrderMapper;
import com.lanf.api.storage.model.dto.AfterSalesIntStockDTO;
import com.lanf.storage.model.entity.*;
import com.lanf.api.storage.mq.constant.StorageClientTopicName;
import com.lanf.api.storage.mq.message.AfterSalesInStockFinishMessage;
import com.lanf.storage.service.stock.IStockFlowService;
import com.lanf.storage.service.stock.IStockService;
import com.lanf.storage.service.storage.IAfterSalesIntStockOrderService;
import com.lanf.storage.service.storage.IInOutStockOrderItemService;
import com.lanf.storage.service.warehous.IWarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 售后出库单 服务实现类
 * </p>
 *
 * @author jarven
 * @since 2026-04-24
 */
@Service
public class AftersalesIntStockOrderServiceImpl extends ServiceImpl<AftersalesIntStockOrderMapper, AfterSalesIntStockOrderDO> implements IAfterSalesIntStockOrderService {


    @Autowired
    private IInOutStockOrderItemService iInOutStockOrderItemService;

    @Autowired
    private RocketMqClient rocketMqClient;

    @Autowired
    private IWarehouseService warehouseService;

    @Autowired
    private IStockService stockService;

    @Autowired
    private IStockFlowService stockFlowService;

    @Autowired
    private IInOutStockOrderItemService storageOrderItemDetailsService;



    @Override
    public void addAfterSalesIntStockOrder(SalesInStockOrderAddMessage message) {

        List<InOutStockOrderItemDO> inOutStockOrderItemDOList = new ArrayList<>();

        AfterSalesIntStockOrderDO one = this.lambdaQuery().eq(AfterSalesIntStockOrderDO::
                getAfterSalesOrderId, message.getAfterSalesOrderId()).one();
        if (one != null) {
            throw new BizException("售后入库单已存在");
        }

        List<SalesInStockOrderItemAdd> salesInStockOrderItemAddDTOList = message.getSalesInStockOrderItemAddDTOList();
        int totalQuantity = 0;

        for (SalesInStockOrderItemAdd sales : salesInStockOrderItemAddDTOList) {
            totalQuantity = totalQuantity + sales.getQuantity();

        }
        Long id = IdUtils.generateId();
        AfterSalesIntStockOrderDO afterSalesIntStockOrderDO = new AfterSalesIntStockOrderDO();
        afterSalesIntStockOrderDO.setId(id);
        afterSalesIntStockOrderDO.setCode(message.getAfterSalesOrderId().toString());
        afterSalesIntStockOrderDO.setAfterSalesOrderId(message.getAfterSalesOrderId());
        afterSalesIntStockOrderDO.setExpectQuantity(totalQuantity);
        afterSalesIntStockOrderDO.setStorageStatus(0);
        afterSalesIntStockOrderDO.setTenantId(message.getTenantId());
        afterSalesIntStockOrderDO.setAfterSalesOrderId(message.getAfterSalesOrderId());
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
            inOutStockOrderItemDO.setTenantId(message.getTenantId());
            inOutStockOrderItemDOList.add(inOutStockOrderItemDO);
        });

        //进行保存
        this.save(afterSalesIntStockOrderDO);
        iInOutStockOrderItemService.saveBatch(inOutStockOrderItemDOList);
    }

    @Transactional
    @Override
    public void inStock(AfterSalesIntStockDTO dto) {

        Long id = dto.getId();

        AfterSalesIntStockOrderDO orderDO = this.getById(id);
        if (orderDO == null) {
            log.error("销售入库单不存在");
            throw new BizException("销售入库单不存在");
        }

        if (StorageStatusEnum.COMPLETED.equals( StorageStatusEnum.getByCode(orderDO.getStorageStatus()))){

            log.warn("销售入库单已入库");
            throw new BizException("销售入库单已入库");
        }
        if (!StorageStatusEnum.WAIT_OUTBOUND.equals( StorageStatusEnum.getByCode(orderDO.getStorageStatus()))){

            log.warn("非待入库状态");
            throw new BizException("非待入库状态");
        }


        List<InOutStockOrderItemDO> list = storageOrderItemDetailsService.lambdaQuery()
                .eq(InOutStockOrderItemDO::getInOutStockOrderId, id)
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

        List<StockDO> updateStockDO = new ArrayList<>();
        List<StockFlowDO> flowDOList = new ArrayList<>();
        List<InOutStockOrderItemDO> updatedItems = new ArrayList<>();

        for (InOutStockOrderItemDO itemDO : list){
            StockDO stockDO = stockMap.get(itemDO.getSkuCode() + ":" + dto.getWarehouseId());
            Integer beforeQuantity = stockDO.getPreStock()+stockDO.getUsableStock();
            Integer changeQuantity = itemDO.getTotalQuantity();
            Integer afterQuantity = beforeQuantity + itemDO.getTotalQuantity();
            //添加可用库存 售后单不参与预发布库存逻辑
            Integer updateUsableStock = stockDO.getUsableStock() + itemDO.getTotalQuantity();
            stockDO.setUsableStock(updateUsableStock);
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
            stockFlowDO.setBizOrderId(orderDO.getId());
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
                .eq(BaseEntity::getId, id)
                .eq(AfterSalesIntStockOrderDO::getVersion, orderDO.getVersion())
                .set(AfterSalesIntStockOrderDO::getStorageStatus, StorageStatusEnum.COMPLETED.getCode())
                .set(AfterSalesIntStockOrderDO::getVersion, orderDO.getVersion() + 1)
                .set(AfterSalesIntStockOrderDO::getActualQuantity, orderDO.getExpectQuantity())
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
                    .set(StockDO::getUsableStock, stockDO.getUsableStock())
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
        /**
         * 通知售后单入库完成
         */
        AfterSalesInStockFinishMessage message = new AfterSalesInStockFinishMessage();
        message.setAfterSalesOrderId(orderDO.getAfterSalesOrderId());
        rocketMqClient.sendMessage(StorageClientTopicName.AFTER_SALES_IN_STOCK_FINISH_TOPIC, JsonUtils.toJsonString(message));
    }
}
