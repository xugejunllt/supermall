package com.lanf.storage.service.storage.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.cache.aop.DistributedLock;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.common.utils.CodeGenerateUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.model.enums.FlowNoPrefixEnum;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.utils.IdUtils;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.storage.mapper.PurchaseInStockOrderMapper;
import com.lanf.storage.model.bo.StockSaveOrUpdateBO;
import com.lanf.storage.model.bo.StockUpdateBO;
import com.lanf.api.storage.model.dto.InStockItemDTO;
import com.lanf.api.storage.model.dto.InStockPurchaseInStockOrderDTO;
import com.lanf.storage.model.entity.*;
import com.lanf.api.storage.model.query.PurchaseInStockOrderPageQuery;
import com.lanf.api.storage.model.vo.PurchaseInStockOrderDetailVO;
import com.lanf.api.storage.model.vo.PurchaseInStockOrderItemDetailVO;
import com.lanf.api.storage.model.vo.PurchaseInStockOrderPageVO;
import com.lanf.storage.service.purchase.IPurchaseOrderService;
import com.lanf.storage.service.stock.IStockFlowService;
import com.lanf.storage.service.stock.IStockService;
import com.lanf.storage.service.storage.IInOutStockOrderItemService;
import com.lanf.storage.service.storage.IPurchaseInStockOrderService;
import com.lanf.storage.service.supplier.ISupplierService;
import com.lanf.storage.service.warehous.IWarehouseService;
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
    private IStockFlowService stockFlowService;
    @Autowired
    private IStockService stockService;

    @Lazy
    @Autowired
    private IPurchaseOrderService purchaseOrderService;
    @Autowired
    private TransactionTemplate transactionTemplate;

    /**
     * 入库
     */
    @Transactional(rollbackFor = {Exception.class})
    @DistributedLock(key = "#inStorageDTO.purchaseInStockOrderId")
    @Override
    public void inStockPurchaseInStockOrder(InStockPurchaseInStockOrderDTO inStorageDTO) {


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
        //校验
        inStorageCheck(storageOrderDO, inStorageItemList, inStorageDTO);
        /**
         * 提前生成流水id
         */
        inStorageDTO.getInStorageItemList().forEach(a-> a.setStockFlowId(IdUtils.generateId()));
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
        //更新商品库存
        StockSaveOrUpdateBO stockSaveOrUpdateBO = buildStockSaveOrUpdate(inStorageItemList, warehouseDODOMap);
        //生成库存流水
        List<StockFlowDO> stockFlowList = buildStockFlowDO(inStorageItemList, purchaseOrderItemDOMap,
                storageOrderDO, warehouseDODOMap,stockSaveOrUpdateBO.getStockDOIdMap(), stockSaveOrUpdateBO);
        //更新入库单实际库存和状态
        PurchaseInStockOrderDO purchaseStorageOrderDOUpdate = buildPurchaseStorageOrderDO(storageOrderDO, enterQuantity);
        List<StockDO> stockSave = stockSaveOrUpdateBO.getStockSave();
        List<StockUpdateBO> stockUpdate = stockSaveOrUpdateBO.getStockUpdate();

        if (!stockUpdate.isEmpty()) {
            //乐观锁更新 更新商品库存
            stockUpdate.forEach(a -> {
                boolean update = stockService.lambdaUpdate().
                        eq(StockDO::getId, a.getId()).
                        eq(StockDO::getVersion, a.getVersion()).
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

    private void inStorageCheck(PurchaseInStockOrderDO storageOrderDO, List<InStockItemDTO> inStorageItemList, InStockPurchaseInStockOrderDTO inStorageDTO) {

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
            InOutStockOrderItemDO> purchaseOrderItemDOMap, PurchaseInStockOrderDO storageOrderDO,
                                               Map<Long, WarehouseDO> warehouseDODOMap,
                                               Map<String,Long> stockDOIdMap, StockSaveOrUpdateBO stockSaveOrUpdateBO) {
        Map<String, Integer> currentTotalStockMap = stockSaveOrUpdateBO.getCurrentTotalStockMap();
        List<StockFlowDO> stockFlowList = new ArrayList<>();
        for (InStockItemDTO is : inStorageItemList) {
            String key = is.getWarehouseId() + is.getSkuCode();
            WarehouseDO warehouseDO = warehouseDODOMap.get(is.getWarehouseId());
            InOutStockOrderItemDO storageOrderItemDetailsDO = purchaseOrderItemDOMap.get(is.getId());
            StockFlowDO stockFlowDO = new StockFlowDO();
            stockFlowDO.setBizOrderId(storageOrderDO.getId());
            stockFlowDO.setStockId(stockDOIdMap.get(key));
            stockFlowDO.setFlowNo(CodeGenerateUtils.generateFlowNo(FlowNoPrefixEnum.STOCK_FLOW,IdUtils.generateId()+""));
            stockFlowDO.setSkuCode(storageOrderItemDetailsDO.getSkuCode());
            stockFlowDO.setAfterQuantity(currentTotalStockMap.get( key)+is.getActualQuantity());
            stockFlowDO.setWarehouseName(warehouseDO.getName());
            stockFlowDO.setChangeQuantity(is.getActualQuantity());
            stockFlowDO.setBeforeQuantity(currentTotalStockMap.get( key));
            stockFlowDO.setWarehouseId(warehouseDO.getId());
            stockFlowDO.setId(is.getStockFlowId());
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
        //key: WarehouseId+SkuCode value: stockDOId
        Map<String,Long> stockDOIdMap = new HashMap<>();
        // key: WarehouseId+SkuCode value: totalStock
        //当前总库存
        Map<String, Integer> currentTotalStockMap = new HashMap<>();
        for (InStockItemDTO st : inStorageItemList) {
            String key = st.getWarehouseId() + st.getSkuCode();
            StockDO stockDO = stockDOMap.get(key);
            if (stockDO == null) {
                WarehouseDO warehouseDO = warehouseDODOMap.get(st.getWarehouseId());
                Long id = IdUtils.generateId();
                //新增
                StockDO stock = new StockDO();
                stock.setId(id);
                stock.setSkuCode(st.getSkuCode());
                stock.setUsableStock(st.getActualQuantity());
                stock.setPreStock(0);
                stock.setWarehouseId(warehouseDO.getId());
                stock.setGoodsName(st.getGoodsName());
                stock.setUnit(st.getUnit());
                stock.setWarehouseName(warehouseDO.getName());
                stockDOSave.add(stock);
                //
                stockDOIdMap.put(key, id);
                currentTotalStockMap.put(key, 0);
            } else {
                //更新
                //总库存 = 可用库存 + 入库数量 + 锁住的库存
                Integer totalStock = stockDO.getUsableStock()+ stockDO.getPreStock();
                StockUpdateBO stockUpdateBO = getStockUpdateBO(st, stockDO);
                stockDOUpdate.add(stockUpdateBO);
                stockDOIdMap.put(key, stockDO.getId());
                currentTotalStockMap.put(key, totalStock);
            }

        }
        return new StockSaveOrUpdateBO(stockDOSave, stockDOUpdate,stockDOIdMap,currentTotalStockMap);

    }

    private static StockUpdateBO getStockUpdateBO(InStockItemDTO st, StockDO stockDO) {
        Integer totalStock = stockDO.getUsableStock() + st.getActualQuantity();
        StockUpdateBO stockUpdateBO = new StockUpdateBO();
        stockUpdateBO.setId(stockDO.getId());
        stockUpdateBO.setVersion(stockDO.getVersion());

        return stockUpdateBO;
    }

    @Override
    public PageResult<PurchaseInStockOrderPageVO> purchaseInStockOrderPageQuery(PurchaseInStockOrderPageQuery query) {

        IPage<PurchaseInStockOrderDO> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<PurchaseInStockOrderDO> purchaseStorageOrderPage = this.lambdaQuery().
                eq(query.getInStockStatus() != null, PurchaseInStockOrderDO::getStorageStatus, query.getInStockStatus()).
                orderByDesc(BaseEntity::getUpdateTime)
                .page(page);

        if (purchaseStorageOrderPage.getRecords().isEmpty()) {

            return PageResult.emptyResult();
        }


        PageResult<PurchaseInStockOrderPageVO> result = new PageResult<>();
        result.setTotal(purchaseStorageOrderPage.getTotal());
        result.setRecords(BeanCopyUtils.copyBeanList(purchaseStorageOrderPage.getRecords(), PurchaseInStockOrderPageVO.class));
        result.setSize(purchaseStorageOrderPage.getSize());

        return result;
    }

    @Override
    public PurchaseInStockOrderDetailVO purchaseInStockOrderDetailQuery(Long id) {

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
