package com.lanf.storage.service.storage.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.common.utils.CodeGenerateUtils;
import com.lanf.common.utils.IdUtils;
import com.lanf.common.utils.ThreadLocalUtils;
import com.lanf.constant.enums.LogisticsTrackStatusEnum;
import com.lanf.constant.exception.BizException;
import com.lanf.aftersales.mq.message.SalesInStockOrderAddMessage;
import com.lanf.aftersales.mq.message.SalesInStockOrderItemAdd;
import com.lanf.messagemanager.client.service.ISendMqMessageService;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.mybatis.base.PageResult;
import com.lanf.order.api.OrderApiService;
import com.lanf.order.model.vo.OrderItemVO;
import com.lanf.order.model.vo.OrderVO;
import com.lanf.rocketmq.model.message.LogisticsTrackBathAddDTO;
import com.lanf.rocketmq.model.message.OutStockFinishEventMessage;
import com.lanf.rocketmq.util.MessageBuildAdapter;
import com.lanf.rocketmq.util.RocketMqClient;
import com.lanf.security.utils.UserUtils;
import com.lanf.storage.mapper.SalesOutStockOrderMapper;
import com.lanf.storage.model.bo.StockUpdateBO;
import com.lanf.storage.model.dto.OutStockDTO;
import com.lanf.storage.model.dto.OutStockItemDTO;
import com.lanf.storage.model.entity.*;
import com.lanf.storage.model.query.SalesOutStockOrderPageQuery;
import com.lanf.storage.model.vo.PurchaseInStockOrderItemDetailVO;
import com.lanf.storage.model.vo.SalesOutStockOrderDetailVO;
import com.lanf.storage.model.vo.SalesOutStockOrderPageVO;
import com.lanf.storage.service.stock.IStockFlowService;
import com.lanf.storage.service.stock.IStockService;
import com.lanf.storage.service.storage.IInOutStockOrderItemService;
import com.lanf.storage.service.storage.ISalesOutStockOrderService;
import com.lanf.storage.service.storage.IStorageFlowService;
import com.lanf.storage.service.warehous.IWarehouseService;
import com.lanf.system.api.SystemService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
    private IStorageFlowService storageFlowService;
    @Autowired
    private IStockFlowService stockFlowService;
    @Autowired
    private IInOutStockOrderItemService storageOrderItemDetailsService;
    @Autowired
    private OrderApiService orderApiService;
    @Autowired
    private RocketMqClient rocketMqClient;
    @Autowired
    private SystemService systemService;
    @Autowired
    private ISendMqMessageService sendMqMessageService;


    @Transactional
    @Override
    public void salesOutStockOrderAdd(Long orderId) {

        /**
         * 远程调用订单服务 查询订单信息
         */
        List<OrderVO> data = orderApiService.queryByOrderId(Arrays.asList(orderId)).getData();
        if (data == null || data.isEmpty()) {
            throw new BizException("订单信息查询异常");
        }
        OrderVO orderVO = data.get(0);
        SalesOutStockOrderDO one = this.lambdaQuery().ge(SalesOutStockOrderDO::getOrderId, orderVO.getOrderId()).one();
        if (one != null) {
            throw new BizException("订单出库单已存在");
        }

        List<InOutStockOrderItemDO> inOutStockOrderItemDOList = new ArrayList<>();

        Long id = IdUtils.generateId();
        SalesOutStockOrderDO salesOutStockOrderDO = new SalesOutStockOrderDO();
        salesOutStockOrderDO.setId(id);
        salesOutStockOrderDO.setCode(CodeGenerateUtils.generaCode());
        salesOutStockOrderDO.setOrderId(orderVO.getOrderId());
        salesOutStockOrderDO.setExpectQuantity(orderVO.getTotalQuantity());
        salesOutStockOrderDO.setActualQuantity(0);
        salesOutStockOrderDO.setStorageStatus(0);
        salesOutStockOrderDO.setExpressCompany(orderVO.getExpressCompany());
        salesOutStockOrderDO.setShopId(orderVO.getShopId());
        salesOutStockOrderDO.setWarehouseId(getWarehouseId(orderVO.getShopId()));
        //
        List<OrderItemVO> inOutStockOrderItemDTOList = orderVO.getInOutStockOrderItemDTOList();
        inOutStockOrderItemDTOList.forEach(b -> {
            //
            InOutStockOrderItemDO inOutStockOrderItemDO = new InOutStockOrderItemDO();
            inOutStockOrderItemDO.setGoodsName(b.getGoodsName());
            inOutStockOrderItemDO.setSkuCode(b.getSkuCode());
            inOutStockOrderItemDO.setTotalQuantity(b.getQuantity());
            inOutStockOrderItemDO.setSurplusQuantity(b.getQuantity());
            inOutStockOrderItemDO.setUnit(b.getUnit());
            inOutStockOrderItemDO.setInOutStockOrderId(id);
            inOutStockOrderItemDOList.add(inOutStockOrderItemDO);
        });

        //进行保存
        this.save(salesOutStockOrderDO);
        iInOutStockOrderItemService.saveBatch(inOutStockOrderItemDOList);
//        /**
//         * 发送mq给物流服务
//         */
//        Integer code = LogisticsTrackStatusEnum.PLACE_AN_ORDER_PLATFORM_INCOME.getCode();
//        String finishContent = "待拣货";
//        String key = orderVO.getOrderId() + ":" + finishContent + ":" + code;
//        LogisticsTrackBathAddDTO logisticsTrackBathAddDTO = MessageBuildAdapter.buildLogisticsTrackAddDTO(orderVO.getOrderId(), finishContent, code);
//        logisticsTrackBathAddDTO.setBizKeyValue(key);
//        sendMqMessageService.sendMessage(TopicName.BATH_ADD_LOGISTICS_TRACK_TOPIC, logisticsTrackBathAddDTO);
    }


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
        salesOutStockOrderDO.setActualQuantity(0);
        salesOutStockOrderDO.setStorageStatus(0);
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

    private Long getWarehouseId(boolean ignoreTableName) {
        //获取仓库id
        ThreadLocalUtils.addIgnoreTableName(ignoreTableName);
        WarehouseDO one = warehouseService.lambdaQuery().one();

        return one.getId();

    }

    private Long getWarehouseId(Long shopId) {
        String tenantCode = systemService.getTenantCodeByShopId(shopId).getData();
        ThreadLocalUtils.addIgnoreTableName(true);
        WarehouseDO warehouseDO = warehouseService.lambdaQuery().
                one();


        return warehouseDO.getId();
    }

    @Transactional
    @Override
    public void outStock(OutStockDTO dto) {

        Long salesOutStockOrderId = dto.getSalesOutStockOrderId();
        List<OutStockItemDTO> outStockItemList = dto.getOutStockItemList();
        SalesOutStockOrderDO salesOutStockOrderDO = this.getById(salesOutStockOrderId);
        Long shopId = salesOutStockOrderDO.getShopId();
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
        //生成入库明细
        StorageFlowDO storageDetailsDO = buildStorageDetailsDO(warehouseDO, salesOutStockOrderDO, totalQuantity);
        //生成库存流水

        List<StockFlowDO> stockFlowList = buildStockFlowDO(outStockItemList, purchaseOrderItemDOMap, salesOutStockOrderDO, warehouseDO);
        //更新入库单实际库存和状态
        SalesOutStockOrderDO purchaseStorageOrderDOUpdate = buildPurchaseStorageOrderDO(salesOutStockOrderDO, totalQuantity);
        //更新商品库存
        List<StockUpdateBO> stockUpdate = buildStockUpdate(outStockItemList);
        //数据库操作
        //更新入库单
        this.updateById(purchaseStorageOrderDOUpdate);
        //更新入库单item数量
        iInOutStockOrderItemService.updateBatchById(storageOrderItemDetailsDOUpdate);
        //保存入库详细
        storageFlowService.save(storageDetailsDO);
        if (!stockUpdate.isEmpty()) {
            stockUpdate.forEach(a -> {
                stockService.lambdaUpdate().
                        eq(StockDO::getId, a.getId()).
                        set(StockDO::getTotalStock, a.getTotalStock());

            });
        }
        //保存库存流水
        stockFlowService.saveBatch(stockFlowList);
        Integer inStorageStatus = getInStorageStatus(salesOutStockOrderDO, totalQuantity);
        if (inStorageStatus == 2 ) {
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


    }


    private void outStockCheck(SalesOutStockOrderDO salesOutStockOrderDO, List<OutStockItemDTO> inStorageItemList, Long salesOutStockOrderId) {

        if (salesOutStockOrderDO == null) {
            throw new BizException("出库单不存在");
        }
        if (salesOutStockOrderDO.getStorageStatus() == 2) {

            throw new BizException("已完成出库");
        }

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
            if (a.getActualQuantity() > stockDO.getTotalStock()) {
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

    private StorageFlowDO buildStorageDetailsDO(WarehouseDO warehouseDO, SalesOutStockOrderDO salesOutStockOrderDO
            , Integer outQuantity) {

        StorageFlowDO storageDetailsDO = new StorageFlowDO();
//        Integer orderType = getOrderType(inOutStatus);
//        storageDetailsDO.setOrderType(getOrderType(inOutStatus));
//        storageDetailsDO.setBizNumber(salesOutStockOrderDO.getCode());
//        if (orderType == 0 || orderType == 3) {
//            storageDetailsDO.setOutQuantity(outQuantity);
//        } else {
//            storageDetailsDO.setInQuantity(outQuantity);
//        }

        return storageDetailsDO;
    }

    private Integer getOrderType(Integer inOutStatus) {

        return inOutStatus;
    }

    private List<StockFlowDO> buildStockFlowDO(List<OutStockItemDTO> inStorageItemList, Map<Long,
            InOutStockOrderItemDO> purchaseOrderItemDOMap, SalesOutStockOrderDO salesOutStockOrderDO, WarehouseDO warehouseDO) {

        List<StockFlowDO> stockFlowList = new ArrayList<>();
        for (OutStockItemDTO is : inStorageItemList) {
            InOutStockOrderItemDO storageOrderItemDetailsDO = purchaseOrderItemDOMap.get(is.getId());
            StockFlowDO stockFlowDO = new StockFlowDO();
//            stockFlowDO.setOrderType(orderType);
//            stockFlowDO.setSkuCode(storageOrderItemDetailsDO.getSkuCode());
//            stockFlowDO.setBizNumber(salesOutStockOrderDO.getCode());
//            if (orderType == 0 || orderType == 3) {
//                stockFlowDO.setOutQuantity(is.getActualQuantity());
//            } else {
//                stockFlowDO.setInQuantity(is.getActualQuantity());
//            }
            stockFlowList.add(stockFlowDO);
        }
        return stockFlowList;
    }

    private SalesOutStockOrderDO buildPurchaseStorageOrderDO(SalesOutStockOrderDO storageOrderDO, Integer enterQuantity) {

        Integer actualStorageQuantity = getActualStorageQuantity(storageOrderDO, enterQuantity);
        Integer status = getInStorageStatus(storageOrderDO, enterQuantity);
        SalesOutStockOrderDO purchaseStorageOrderDOUpdate = new SalesOutStockOrderDO();
        purchaseStorageOrderDOUpdate.setId(storageOrderDO.getId());
        purchaseStorageOrderDOUpdate.setStorageStatus(status);
        purchaseStorageOrderDOUpdate.setActualQuantity(actualStorageQuantity);
        return purchaseStorageOrderDOUpdate;
    }

    private Integer getActualStorageQuantity(SalesOutStockOrderDO storageOrderDO, Integer outQuantity) {

        return storageOrderDO.getActualQuantity() + outQuantity;
    }

    /**
     * 获取入库状态
     * 1:部分出库
     * 2:全部出库
     */
    private Integer getInStorageStatus(SalesOutStockOrderDO storageOrderDO, Integer enterQuantity) {

        Integer actualStorageQuantity = getActualStorageQuantity(storageOrderDO, enterQuantity);
        Integer status = null;
        if (actualStorageQuantity.equals(storageOrderDO.getExpectQuantity())) {
            status = 2;
        } else {
            status = 1;
        }
        return status;
    }

    private List<StockUpdateBO> buildStockUpdate(List<OutStockItemDTO> inStorageItemList) {

        List<String> skuCodeList = inStorageItemList.stream().map(OutStockItemDTO::getSkuCode).collect(Collectors.toList());
        ThreadLocalUtils.addIgnoreTableName(true);
        List<StockDO> stockDOlist = stockService.lambdaQuery().in(StockDO::getSkuCode, skuCodeList).list();
        Map<String, StockDO> stockDOMap = stockDOlist.stream()
                .collect(Collectors.toMap(StockDO::getSkuCode, Function.identity()));
        List<StockUpdateBO> stockDOUpdate = new ArrayList<>();
        for (OutStockItemDTO st : inStorageItemList) {
            String skuCode = st.getSkuCode();
            StockDO stockDO = stockDOMap.get(skuCode);
            //更新
            Integer totalStock =  stockDO.getTotalStock() - st.getActualQuantity();
            Integer usableStock = stockDO.getTotalStock() + st.getActualQuantity();

            StockUpdateBO stockUpdateBO = new StockUpdateBO();
            stockUpdateBO.setTotalStock(totalStock);
            stockUpdateBO.setUsableStock(usableStock);
            stockUpdateBO.setId(stockDO.getId());
            stockDOUpdate.add(stockUpdateBO);

        }
        return stockDOUpdate;

    }

    @Override
    public PageResult<SalesOutStockOrderPageVO> salesOutStockOrderPage(SalesOutStockOrderPageQuery query) {

        IPage<SalesOutStockOrderDO> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<SalesOutStockOrderDO> purchaseStorageOrderPage = this.lambdaQuery().
                eq(SalesOutStockOrderDO::getShopId, UserUtils.getShopId()).
                eq(query.getInStockStatus() != null, SalesOutStockOrderDO::getStorageStatus, query.getInStockStatus()).
                eq(!ObjectUtils.isEmpty(query.getOrderId()), SalesOutStockOrderDO::getOrderId, query.getOrderId()).
                orderByDesc(BaseEntity::getUpdateTime)
                .page(page);

        if (purchaseStorageOrderPage.getRecords().isEmpty()) {

            return PageResult.emptyResult(SalesOutStockOrderPageVO.class);
        }

        PageResult<SalesOutStockOrderPageVO> pageResult = PageResult.toPageResult(page, SalesOutStockOrderPageVO.class);

        List<SalesOutStockOrderDO> records = purchaseStorageOrderPage.getRecords();
        /**
         * 填充关联属性
         */
        //用set接收 去重
        Set<Long> warehouseIdList = records.stream().map(SalesOutStockOrderDO::getWarehouseId).collect(Collectors.toSet());
        ThreadLocalUtils.addIgnoreTableName(true);

        Map<Long, WarehouseDO> warehouseMap = warehouseService.lambdaQuery().in(WarehouseDO::getId, warehouseIdList).list().stream().
                collect(Collectors.toMap(WarehouseDO::getId, Function.identity()));

        pageResult.getRecords().forEach(vo -> {
            WarehouseDO warehouseDO = warehouseMap.get(vo.getWarehouseId());
            if (warehouseDO != null) {
                vo.setWarehouseName(warehouseDO.getName());
            }

        });

        return pageResult;
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
        Long warehouseId = storageOrderDO.getWarehouseId();
        ThreadLocalUtils.addIgnoreTableName(true);
        String warehouseName = warehouseService.getById(warehouseId).getName();

        Integer totalExpectStorageQuantity = storageOrderDO.getExpectQuantity();
        Integer totalActualStorageQuantity = storageOrderDO.getActualQuantity();
        Integer totalActualSurplusQuantity = totalExpectStorageQuantity - totalActualStorageQuantity;
        List<PurchaseInStockOrderItemDetailVO> purchaseStorageOrderItemDetailVOList =
                BeanCopyUtils.copyBeanList(storageOrderItemDetailsList, PurchaseInStockOrderItemDetailVO.class);
        purchaseStorageOrderItemDetailVOList.forEach(a -> {
            //实际入库数量 = 总数量-剩余数量
            a.setActualQuantity(a.getTotalQuantity() - a.getSurplusQuantity());
        });
        SalesOutStockOrderDetailVO purchaseStorageOrderDetailVO = new SalesOutStockOrderDetailVO();
        BeanCopyUtils.copy(storageOrderDO, purchaseStorageOrderDetailVO);
        purchaseStorageOrderDetailVO.setTotalExpectStorageQuantity(totalExpectStorageQuantity);
        purchaseStorageOrderDetailVO.setTotalActualStorageQuantity(totalActualStorageQuantity);
        purchaseStorageOrderDetailVO.setTotalActualSurplusQuantity(totalActualSurplusQuantity);
        purchaseStorageOrderDetailVO.setCode(storageOrderDO.getCode());
        purchaseStorageOrderDetailVO.setPurchaseStorageOrderItemDetailVOList(purchaseStorageOrderItemDetailVOList);
        purchaseStorageOrderDetailVO.setWarehouseName(warehouseName);
        return purchaseStorageOrderDetailVO;
    }
}
