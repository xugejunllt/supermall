package com.lanf.storage.service.storage.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.aftersales.mq.message.SalesInStockOrderAddMessage;
import com.lanf.aftersales.mq.message.SalesInStockOrderItemAdd;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.common.utils.CodeGenerateUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.common.utils.ThreadLocalUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.model.enums.LogisticsTrackStatusEnum;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.utils.IdUtils;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.order.api.OrderApiService;
import com.lanf.rocketmq.model.message.LogisticsTrackBathAddDTO;
import com.lanf.rocketmq.model.message.OutStockFinishEventMessage;
import com.lanf.rocketmq.util.MessageBuildAdapter;
import com.lanf.rocketmq.util.RocketMqClient;
import com.lanf.storage.mapper.SalesOutStockOrderMapper;
import com.lanf.storage.model.bo.StockUpdateBO;
import com.lanf.storage.model.dto.OutStockItemDTO;
import com.lanf.storage.model.dto.OutStockSalesOutStockOrderDTO;
import com.lanf.storage.model.entity.*;
import com.lanf.storage.model.enums.StorageStatusEnum;
import com.lanf.storage.model.query.SalesOutStockOrderPageQuery;
import com.lanf.storage.model.vo.PurchaseInStockOrderItemDetailVO;
import com.lanf.storage.model.vo.SalesOutStockOrderDetailVO;
import com.lanf.storage.model.vo.SalesOutStockOrderPageVO;
import com.lanf.storage.mq.constant.StorageClientTopicName;
import com.lanf.storage.mq.message.SalesOutStockOrderFinishMessage;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private OrderApiService orderApiService;
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
        List<OutStockItemDTO> outStockItemList = dto.getOutStockItemList();
        SalesOutStockOrderDO salesOutStockOrderDO = this.getById(salesOutStockOrderId);
        List<InOutStockOrderItemDO> storageOrderItemDetailList = iInOutStockOrderItemService.lambdaQuery().
                eq(InOutStockOrderItemDO::getInOutStockOrderId, salesOutStockOrderId).list();
        Map<Long, InOutStockOrderItemDO> purchaseOrderItemDOMap = storageOrderItemDetailList.stream()
                .collect(Collectors.toMap(InOutStockOrderItemDO::getId, Function.identity()));
        //校验
        outStockCheck(salesOutStockOrderDO, outStockItemList, salesOutStockOrderId);
        //构建更新StorageOrderItemDetailsDO
        List<InOutStockOrderItemDO> storageOrderItemDetailsDOUpdate = buildStorageOrderItemDetailsDO(outStockItemList,
                purchaseOrderItemDOMap);
        //出入库总数量
        Integer totalQuantity = 0;
        for (OutStockItemDTO is : outStockItemList) {
            totalQuantity += is.getActualQuantity();
        }
        WarehouseDO warehouseDO = warehouseService.getById(getWarehouseId());

        //生成库存流水
        List<StockFlowDO> stockFlowList = buildStockFlowDO(outStockItemList, purchaseOrderItemDOMap, salesOutStockOrderDO, warehouseDO);
        //更新商品库存
        List<StockUpdateBO> stockUpdate = buildStockUpdate(outStockItemList);
        //更新入库单
        SalesOutStockOrderDO one = this.lambdaQuery().eq(SalesOutStockOrderDO::getId, salesOutStockOrderId).one();
        if (one == null){
            log.error("入库单不存在");
            throw new BizException("入库单不存在");
        }
        boolean update1 = this.lambdaUpdate().eq(BaseEntity::getId, salesOutStockOrderId)
                .eq(SalesOutStockOrderDO::getVersion, one.getVersion())
                .set(SalesOutStockOrderDO::getVersion, one.getVersion() + 1)
                .update();
        if (!update1) {
            throw new BizException("更新入库单失败");
        }
        //更新入库单item数量
        iInOutStockOrderItemService.updateBatchById(storageOrderItemDetailsDOUpdate);
        stockUpdate.forEach(a -> {
            boolean update = stockService.lambdaUpdate().
                    eq(StockDO::getId, a.getId())
                    .eq(StockDO::getVersion, a.getVersion())
                    .set(StockDO::getPreStock, a.getLockStock())
                    .set(StockDO::getVersion, a.getVersion() + 1)
                    .update();
            if (!update) {
                throw new BizException("更新库存失败");
            }
        });
        //保存库存流水
        stockFlowService.saveBatch(stockFlowList);
        Integer inStorageStatus = null;
        if (inStorageStatus == 2) {
            //销售单出库完成
            log.info("出库完成");
            String finishContent = "打包完成";
            String key = salesOutStockOrderDO.getOrderId() + ":" + finishContent;
            OutStockFinishEventMessage message = new OutStockFinishEventMessage();

            // 更新订单状态
            message.setOrderId(salesOutStockOrderDO.getOrderId());
            //添加物流状态
            LogisticsTrackBathAddDTO logisticsTrackBathAddDTO = MessageBuildAdapter.buildLogisticsTrackAddDTO(salesOutStockOrderDO.getOrderId(), finishContent,
                    LogisticsTrackStatusEnum.PLACE_AN_ORDER_PLATFORM_INCOME.getCode());
            message.setLogisticsTrackBathAddDTO(logisticsTrackBathAddDTO);
            //sendMqMessageService.sendMessage(TopicName.OUT_STOCK_FINISH_EVENT_TOPIC, message);
        }
        /**
         * 出库完成 发送给订单消息 --更新为已出库状态
         */
        SalesOutStockOrderFinishMessage message = new SalesOutStockOrderFinishMessage();
        message.setOrderId(salesOutStockOrderDO.getOrderId());
        rocketMqClient.sendMessage(StorageClientTopicName.SALES_OUT_STOCK_ORDER_FINISH_TOPIC, JsonUtils.toJsonString(message));

    }


    private void outStockCheck(SalesOutStockOrderDO salesOutStockOrderDO, List<OutStockItemDTO> inStorageItemList, Long salesOutStockOrderId) {

        if (salesOutStockOrderDO == null) {
            throw new BizException("出库单不存在");
        }
//        if (salesOutStockOrderDO.getStorageStatus() == 1) {
//
//            throw new BizException("已完成出库");
//        }

        Set<Long> purchaseOrderItemIdSet = inStorageItemList.stream().map(OutStockItemDTO::getId).collect(Collectors.toSet());
        List<InOutStockOrderItemDO> storageOrderItemDetailList = iInOutStockOrderItemService.lambdaQuery().
                eq(InOutStockOrderItemDO::getInOutStockOrderId, salesOutStockOrderId).list();

        if (purchaseOrderItemIdSet.size() != storageOrderItemDetailList.size()) {
            throw new BizException("入库单部分商品数据不存在");
        }
        Map<Long, InOutStockOrderItemDO> purchaseOrderItemDOMap = storageOrderItemDetailList.stream()
                .collect(Collectors.toMap(InOutStockOrderItemDO::getId, Function.identity()));
        //校验实际出库数量是否合法
        inStorageItemList.forEach(a -> {
            Integer actualQuantity = a.getActualQuantity();
            Integer quantity = purchaseOrderItemDOMap.get(a.getId()).getSurplusQuantity();
            if (actualQuantity > quantity) {
                throw new BizException("入库商品" + a.getId() + "实际出库数量大于剩余数量");
            }
        });
        //校验库存是否足够
        List<String> skuCodeList = inStorageItemList.stream().map(OutStockItemDTO::getSkuCode).collect(Collectors.toList());
        ThreadLocalUtils.addIgnoreTableName(true);
        List<StockDO> StockDOList = stockService.lambdaQuery().in(StockDO::getSkuCode, skuCodeList).list();
        Map<String, StockDO> StockDOMap = StockDOList.stream()
                .collect(Collectors.toMap(StockDO::getSkuCode, Function.identity()));
        inStorageItemList.forEach(a -> {
            String skuCode = a.getSkuCode();
            StockDO stockDO = StockDOMap.get(skuCode);
            if (stockDO == null) {
                throw new BizException("商品" + skuCode + "不存在");
            }
            /**
             * 扣减锁住的库存
             */
            if (a.getActualQuantity() > stockDO.getPreStock()) {
                throw new BizException("商品" + skuCode + "库存不足");
            }

        });


    }

    private List<InOutStockOrderItemDO> buildStorageOrderItemDetailsDO(List<OutStockItemDTO> inStorageItemList,
                                                                       Map<Long, InOutStockOrderItemDO> purchaseOrderItemDOMap) {

        List<InOutStockOrderItemDO> storageOrderItemDetailsDOUpdate = new ArrayList<>();
        for (OutStockItemDTO s : inStorageItemList) {

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





    private List<StockFlowDO> buildStockFlowDO(List<OutStockItemDTO> inStorageItemList, Map<Long,
            InOutStockOrderItemDO> purchaseOrderItemDOMap, SalesOutStockOrderDO salesOutStockOrderDO, WarehouseDO warehouseDO) {

        List<StockFlowDO> stockFlowList = new ArrayList<>();
        for (OutStockItemDTO is : inStorageItemList) {
            InOutStockOrderItemDO storageOrderItemDetailsDO = purchaseOrderItemDOMap.get(is.getId());
            StockFlowDO stockFlowDO = new StockFlowDO();
            stockFlowDO.setBizOrderId(storageOrderItemDetailsDO.getInOutStockOrderId());
            stockFlowDO.setOrderId(salesOutStockOrderDO.getOrderId());
//            stockFlowDO.setStockId(storageOrderItemDetailsDO.getStockId());

            stockFlowList.add(stockFlowDO);
        }
        return stockFlowList;
    }






    private List<StockUpdateBO> buildStockUpdate(List<OutStockItemDTO> inStorageItemList) {

        List<String> skuCodeList = inStorageItemList.stream().map(OutStockItemDTO::getSkuCode).collect(Collectors.toList());
        List<StockDO> stockDOlist = stockService.lambdaQuery().in(StockDO::getSkuCode, skuCodeList).list();
        Map<String, StockDO> stockDOMap = stockDOlist.stream()
                .collect(Collectors.toMap(StockDO::getSkuCode, Function.identity()));
        List<StockUpdateBO> stockDOUpdate = new ArrayList<>();
        for (OutStockItemDTO st : inStorageItemList) {
            String skuCode = st.getSkuCode();
            StockDO stockDO = stockDOMap.get(skuCode);
            //更新
            Integer lockStock = stockDO.getPreStock() - st.getActualQuantity();
            StockUpdateBO stockUpdateBO = new StockUpdateBO();
            stockUpdateBO.setLockStock(lockStock);
            stockUpdateBO.setId(stockDO.getId());
            stockUpdateBO.setVersion(stockDO.getVersion());
            stockDOUpdate.add(stockUpdateBO);

        }
        return stockDOUpdate;

    }

    @Override
    public PageResult<SalesOutStockOrderPageVO> salesOutStockOrderPageQuery(SalesOutStockOrderPageQuery query) {

        IPage<SalesOutStockOrderDO> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<SalesOutStockOrderDO> purchaseStorageOrderPage = this.lambdaQuery().
                eq(query.getInStockStatus() != null, SalesOutStockOrderDO::getStorageStatus, query.getInStockStatus()).
                eq(!ObjectUtils.isEmpty(query.getOrderId()), SalesOutStockOrderDO::getOrderId, query.getOrderId()).
                orderByDesc(BaseEntity::getUpdateTime)
                .page(page);

//        if (purchaseStorageOrderPage.getRecords().isEmpty()) {
//
//            return PageResult.emptyResult(SalesOutStockOrderPageVO.class);
//        }
//
//        PageResult<SalesOutStockOrderPageVO> pageResult = PageResult.toPageResult(page, SalesOutStockOrderPageVO.class);

        List<SalesOutStockOrderDO> records = purchaseStorageOrderPage.getRecords();
        /**
         * 填充关联属性
         */
//        //用set接收 去重
//        Set<Long> warehouseIdList = records.stream().map(SalesOutStockOrderDO::getWarehouseId).collect(Collectors.toSet());
//        ThreadLocalUtils.addIgnoreTableName(true);
//
//        Map<Long, WarehouseDO> warehouseMap = warehouseService.lambdaQuery().in(WarehouseDO::getId, warehouseIdList).list().stream().
//                collect(Collectors.toMap(WarehouseDO::getId, Function.identity()));
//
//        pageResult.getRecords().forEach(vo -> {
//            WarehouseDO warehouseDO = warehouseMap.get(vo.getWarehouseId());
//            if (warehouseDO != null) {
//                vo.setWarehouseName(warehouseDO.getName());
//            }
//
//        });

        return null;
    }

    @Override
    public SalesOutStockOrderDetailVO salesOutStockOrderDetail(Long id) {

        SalesOutStockOrderDO storageOrderDO = this.getById(id);
        if (storageOrderDO == null) {
            throw new BizException("销售出库单不存在");
        }
        List<InOutStockOrderItemDO> storageOrderItemDetailsList = storageOrderItemDetailsService.
                lambdaQuery().eq(InOutStockOrderItemDO::getInOutStockOrderId, id).list();
        if (storageOrderItemDetailsList.isEmpty()) {
            throw new BizException("销售出库单商品不存在");
        }
        String warehouseName =null;

        List<PurchaseInStockOrderItemDetailVO> purchaseStorageOrderItemDetailVOList =
                BeanCopyUtils.copyBeanList(storageOrderItemDetailsList, PurchaseInStockOrderItemDetailVO.class);
        purchaseStorageOrderItemDetailVOList.forEach(a -> {
            //实际入库数量 = 总数量-剩余数量
            a.setActualQuantity(a.getTotalQuantity() - a.getSurplusQuantity());
        });
        SalesOutStockOrderDetailVO purchaseStorageOrderDetailVO = new SalesOutStockOrderDetailVO();
        BeanCopyUtils.copy(storageOrderDO, purchaseStorageOrderDetailVO);
        purchaseStorageOrderDetailVO.setCode(storageOrderDO.getCode());
        purchaseStorageOrderDetailVO.setPurchaseStorageOrderItemDetailVOList(purchaseStorageOrderItemDetailVOList);
        purchaseStorageOrderDetailVO.setWarehouseName(warehouseName);
        return purchaseStorageOrderDetailVO;
    }
}
