package com.lanf.storage.service.storage.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.api.storage.model.dto.InStockItemDTO;
import com.lanf.api.storage.model.dto.InStockPurchaseInStockOrderDTO;
import com.lanf.api.storage.model.enums.StorageStatusEnum;
import com.lanf.api.storage.model.query.PurchaseInStockOrderPageQuery;
import com.lanf.api.storage.model.vo.PurchaseInStockOrderDetailVO;
import com.lanf.api.storage.model.vo.PurchaseInStockOrderItemDetailVO;
import com.lanf.api.storage.model.vo.PurchaseInStockOrderPageVO;
import com.lanf.cache.aop.DistributedLock;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.common.utils.CodeGenerateUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.model.enums.FlowNoPrefixEnum;
import com.lanf.constant.model.enums.storage.StockFlowTypeEnum;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.utils.IdUtils;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.storage.mapper.PurchaseInStockOrderMapper;
import com.lanf.storage.model.bo.StockUpdateBO;
import com.lanf.storage.model.entity.*;
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
         * 1. 准备数据
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
        
        // 校验
        inStorageCheck(storageOrderDO, inStorageItemList, inStorageDTO);
        
        /**
         * 2. 提前生成流水id
         */
        inStorageDTO.getInStorageItemList().forEach(a -> a.setStockFlowId(IdUtils.generateId()));
        
        /**
         * 3. 计算入库总数量
         */
        Integer enterQuantity = inStorageItemList.stream()
                .mapToInt(InStockItemDTO::getActualQuantity)
                .sum();
        
        /**
         * 4. 构建更新入库单明细
         */
        List<InOutStockOrderItemDO> storageOrderItemDetailsDOUpdate = buildStorageOrderItemDetailsDO(inStorageItemList,
                purchaseOrderItemDOMap);
        
        /**
         * 5. 检查库存是否存在（skuCode + warehouseId 唯一），不存在则插入，存在则更新
         */
        List<StockDO> stockSaveList = new ArrayList<>();
        List<StockUpdateBO> stockUpdateList = new ArrayList<>();
        Map<String, Long> stockIdMap = new HashMap<>();
        Map<String, Integer> beforeStockMap = new HashMap<>();
        
        processStock(inStorageItemList, warehouseDODOMap, stockSaveList, stockUpdateList, stockIdMap, beforeStockMap);
        
        /**
         * 6. 生成库存流水
         */
        List<StockFlowDO> stockFlowList = buildStockFlowDO(inStorageItemList, purchaseOrderItemDOMap,
                storageOrderDO, warehouseDODOMap, stockIdMap, beforeStockMap);
        
        /**
         * 7. 计算入库后的实际数量和状态
         */
        Integer actualStorageQuantity = storageOrderDO.getActualStorageQuantity() + enterQuantity;
        StorageStatusEnum storageStatus = actualStorageQuantity.equals(storageOrderDO.getExpectStorageQuantity())
                ? StorageStatusEnum.COMPLETED
                : StorageStatusEnum.PARTIAL_OUTBOUND;
        
        /**
         * 8. 执行数据库操作
         */
        // 8.1 新增库存
        if (!stockSaveList.isEmpty()) {
            stockService.saveBatch(stockSaveList);
        }
        
        // 8.2 乐观锁更新库存
        if (!stockUpdateList.isEmpty()) {
            stockUpdateList.forEach(stockUpdate -> {
                boolean updateSuccess = stockService.lambdaUpdate()
                        .eq(StockDO::getId, stockUpdate.getId())
                        .eq(StockDO::getVersion, stockUpdate.getVersion())
                        .set(StockDO::getUsableStock, stockUpdate.getUsableStock())
                        .set(StockDO::getVersion, stockUpdate.getVersion() + 1)
                        .update();
                
                if (!updateSuccess) {
                    log.error("更新库存失败，库存ID: {}, 版本号: {}", stockUpdate.getId(), stockUpdate.getVersion());
                    throw new BizException("更新库存失败，请重试");
                }
            });
        }
        
        // 8.3 乐观锁更新入库单（当全部入库时使用版本号控制）
        if (storageStatus == StorageStatusEnum.COMPLETED) {
            boolean updateSuccess = this.lambdaUpdate()
                    .eq(PurchaseInStockOrderDO::getId, storageOrderDO.getId())
                    .eq(PurchaseInStockOrderDO::getVersion, storageOrderDO.getVersion())
                    .set(PurchaseInStockOrderDO::getActualStorageQuantity, actualStorageQuantity)
                    .set(PurchaseInStockOrderDO::getStorageStatus, StorageStatusEnum.COMPLETED)
                    .set(PurchaseInStockOrderDO::getVersion, storageOrderDO.getVersion() + 1)
                    .update();
            
            if (!updateSuccess) {
                log.error("更新入库单为已完成状态失败，入库单ID: {}", storageOrderDO.getId());
                throw new BizException("更新入库单状态失败，请重试");
            }
        } else {
            // 部分入库，普通更新
            PurchaseInStockOrderDO updateDO = new PurchaseInStockOrderDO();
            updateDO.setId(storageOrderDO.getId());
            updateDO.setActualStorageQuantity(actualStorageQuantity);
            updateDO.setStorageStatus(StorageStatusEnum.PARTIAL_OUTBOUND);
            this.updateById(updateDO);
        }
        
        // 8.4 更新入库单明细数量
        storageOrderItemDetailsService.updateBatchById(storageOrderItemDetailsDOUpdate);
        
        // 8.5 保存库存流水
        stockFlowService.saveBatch(stockFlowList);

    }

    /**
     * 处理库存：检查是否存在，不存在则插入，存在则准备更新
     *
     * @param inStorageItemList 入库商品列表
     * @param warehouseDODOMap 仓库映射
     * @param stockSaveList 待保存的库存列表
     * @param stockUpdateList 待更新的库存列表
     * @param stockIdMap 库存ID映射 (key: warehouseId+skuCode)
     * @param beforeStockMap 变更前库存数量映射 (key: warehouseId+skuCode)
     */
    private void processStock(List<InStockItemDTO> inStorageItemList,
                              Map<Long, WarehouseDO> warehouseDODOMap,
                              List<StockDO> stockSaveList,
                              List<StockUpdateBO> stockUpdateList,
                              Map<String, Long> stockIdMap,
                              Map<String, Integer> beforeStockMap) {
        
        // 收集所有需要处理的 skuCode
        List<String> skuCodeList = inStorageItemList.stream()
                .map(InStockItemDTO::getSkuCode)
                .distinct()
                .collect(Collectors.toList());
        
        // 批量查询已存在的库存
        List<StockDO> existingStockList = stockService.lambdaQuery()
                .in(StockDO::getSkuCode, skuCodeList)
                .list();
        
        // 构建库存映射 key: warehouseId + skuCode
        Map<String, StockDO> stockMap = existingStockList.stream()
                .collect(Collectors.toMap(
                        stock -> stock.getWarehouseId() + stock.getSkuCode(),
                        Function.identity()
                ));
        
        // 遍历入库商品，处理库存
        for (InStockItemDTO item : inStorageItemList) {
            String key = item.getWarehouseId() + item.getSkuCode();
            WarehouseDO warehouse = warehouseDODOMap.get(item.getWarehouseId());
            StockDO existingStock = stockMap.get(key);
            
            if (existingStock == null) {
                // 库存不存在，准备插入
                StockDO newStock = new StockDO();
                newStock.setId(IdUtils.generateId());
                newStock.setSkuCode(item.getSkuCode());
                newStock.setGoodsName(item.getGoodsName());
                newStock.setWarehouseId(item.getWarehouseId());
                newStock.setWarehouseName(warehouse.getName());
                newStock.setUnit(item.getUnit());
                newStock.setUsableStock(item.getActualQuantity());
                newStock.setPreStock(0);
                stockSaveList.add(newStock);
                
                // 记录库存ID和变更前数量
                stockIdMap.put(key, newStock.getId());
                beforeStockMap.put(key, 0);
            } else {
                // 库存存在，准备更新 usableStock
                Integer currentUsableStock = existingStock.getUsableStock();
                Integer newUsableStock = currentUsableStock + item.getActualQuantity();
                
                StockUpdateBO updateBO = new StockUpdateBO();
                updateBO.setId(existingStock.getId());
                updateBO.setVersion(existingStock.getVersion());
                updateBO.setUsableStock(newUsableStock);
                stockUpdateList.add(updateBO);
                
                // 记录库存ID和变更前数量
                stockIdMap.put(key, existingStock.getId());
                beforeStockMap.put(key, currentUsableStock);
            }
        }
    }

    /**
     * 入库校验
     */
    private void inStorageCheck(PurchaseInStockOrderDO storageOrderDO, List<InStockItemDTO> inStorageItemList, InStockPurchaseInStockOrderDTO inStorageDTO) {

        if (storageOrderDO == null) {
            throw new BizException("入库单不存在");
        }
        
        // 校验仓库是否存在
        Set<Long> warehouseIdSet = inStorageItemList.stream().map(InStockItemDTO::getWarehouseId).collect(Collectors.toSet());
        int warehouseCount = warehouseService.lambdaQuery().in(BaseEntity::getId, warehouseIdSet).count();
        if (warehouseIdSet.size() != warehouseCount) {
            throw new BizException("仓库id不存在");
        }

        // 校验入库单明细是否存在
        Set<Long> purchaseOrderItemIdSet = inStorageItemList.stream().map(InStockItemDTO::getId).collect(Collectors.toSet());
        List<InOutStockOrderItemDO> storageOrderItemDetailList = storageOrderItemDetailsService.lambdaQuery().
                eq(InOutStockOrderItemDO::getInOutStockOrderId, inStorageDTO.getPurchaseInStockOrderId()).list();

        if (purchaseOrderItemIdSet.size() != storageOrderItemDetailList.size()) {
            throw new BizException("入库单部分商品数据不存在");
        }
        
        Map<Long, InOutStockOrderItemDO> purchaseOrderItemDOMap = storageOrderItemDetailList.stream()
                .collect(Collectors.toMap(InOutStockOrderItemDO::getId, Function.identity()));

        // 校验实际入库数量是否合法
        inStorageItemList.forEach(item -> {
            Integer actualQuantity = item.getActualQuantity();
            Integer surplusQuantity = purchaseOrderItemDOMap.get(item.getId()).getSurplusQuantity();
            if (actualQuantity > surplusQuantity) {
                throw new BizException("入库商品" + item.getId() + "实际入库数量大于剩余数量");
            }
        });
    }

    /**
     * 构建库存流水
     */
    private List<StockFlowDO> buildStockFlowDO(List<InStockItemDTO> inStorageItemList,
                                                Map<Long, InOutStockOrderItemDO> purchaseOrderItemDOMap,
                                                PurchaseInStockOrderDO storageOrderDO,
                                                Map<Long, WarehouseDO> warehouseDODOMap,
                                                Map<String, Long> stockIdMap,
                                                Map<String, Integer> beforeStockMap) {
        List<StockFlowDO> stockFlowList = new ArrayList<>();
        
        for (InStockItemDTO item : inStorageItemList) {
            String key = item.getWarehouseId() + item.getSkuCode();
            WarehouseDO warehouse = warehouseDODOMap.get(item.getWarehouseId());
            InOutStockOrderItemDO orderItem = purchaseOrderItemDOMap.get(item.getId());
            
            Integer beforeQuantity = beforeStockMap.get(key);
            Integer changeQuantity = item.getActualQuantity();
            Integer afterQuantity = beforeQuantity + changeQuantity;
            
            StockFlowDO stockFlow = new StockFlowDO();
            stockFlow.setId(item.getStockFlowId());
            stockFlow.setFlowNo(CodeGenerateUtils.generateFlowNo(FlowNoPrefixEnum.STOCK_FLOW,
                    IdUtils.generateId()+""));
            stockFlow.setStockId(stockIdMap.get(key));
            stockFlow.setFlowType(StockFlowTypeEnum.PURCHASE_INBOUND);
            stockFlow.setBizOrderId(storageOrderDO.getId());
            stockFlow.setSkuCode(orderItem.getSkuCode());
            stockFlow.setBeforeQuantity(beforeQuantity);
            stockFlow.setChangeQuantity(changeQuantity);
            stockFlow.setAfterQuantity(afterQuantity);
            stockFlow.setWarehouseId(warehouse.getId());
            stockFlow.setWarehouseName(warehouse.getName());
            
            stockFlowList.add(stockFlow);
        }
        
        return stockFlowList;
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
