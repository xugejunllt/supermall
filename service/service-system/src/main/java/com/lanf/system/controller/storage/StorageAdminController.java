package com.lanf.system.controller.storage;

import com.lanf.api.storage.api.StorageApiService;
import com.lanf.api.storage.model.dto.*;
import com.lanf.api.storage.model.query.*;
import com.lanf.api.storage.model.vo.*;
import com.lanf.constant.model.query.PageQuery;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/storage")
public class StorageAdminController {

    @Autowired
    private StorageApiService storageApiService;

    // ==================== Warehouse 仓库管理 ====================

    @GetMapping("/warehousePageQuery")
    public Result<PageResult<WarehousePageVO>> warehousePageQuery(WarehousePageQuery query) {
        log.info("分页查询仓库列表:query{}", query);
        return storageApiService.warehousePageQuery(query);
    }

    @PostMapping("/addWarehouse")
    public Result<Void> addWarehouse(@Validated @RequestBody AddWarehouseDTO warehouse) {
        log.info("添加仓库:warehouse{}", warehouse);
        return storageApiService.addWarehouse(warehouse);
    }

    // ==================== Supplier 供应商管理 ====================

    @GetMapping("/supplierPageQuery")
    public Result<PageResult<SupplierPageVO>> supplierPageQuery(SupplierPageQuery query) {
        log.info("分页查询供应商列表:query{}", query);
        return storageApiService.supplierPageQuery(query);
    }

    @PostMapping("/addSupplier")
    public Result<Void> addSupplier(@Validated @RequestBody AddSupplierDTO supplier) {
        log.info("添加供应商:supplier{}", supplier);
        return storageApiService.addSupplier(supplier);
    }

    @GetMapping("/supplierListQuery")
    public Result<List<SupplierListVO>> supplierListQuery() {
        log.info("查询供应商列表");
        return storageApiService.supplierList();
    }

    // ==================== PurchaseOrder 采购单管理 ====================

    @PostMapping("/addPurchaseOrder")
    public Result<Void> addPurchaseOrder(@Validated @RequestBody AddPurchaseOrderDTO purchaseOrderAdd) {
        log.info("添加采购单:purchaseOrderAdd{}", purchaseOrderAdd);
        return storageApiService.addPurchaseOrder(purchaseOrderAdd);
    }

    @GetMapping("/purchaseOrderPageQuery")
    public Result<PageResult<PurchaseOrderPageVO>> purchaseOrderPageQuery(PurchaseOrderPageQuery query) {
        log.info("分页查询采购单列表:query{}", query);
        return storageApiService.purchaseOrderPageQuery(query);
    }

    @GetMapping("/purchaseOrderDetailQuery")
    public Result<PurchaseOrderDetailVO> purchaseOrderDetailQuery(Long id) {
        log.info("采购单详细:id{}", id);
        return storageApiService.purchaseOrderDetailQuery(id);
    }

    @PostMapping("/auditApprove")
    public Result<Void> auditApprove(@RequestBody ReviewDTO dto) {
        log.info("采购单审核:dto{}", dto);
        return storageApiService.auditApprove(dto);
    }

    // ==================== PurchaseInStockOrder 采购入库单管理 ====================

    @PostMapping("/inStockPurchaseInStockOrder")
    public Result<Void> inStockPurchaseInStockOrder(@Validated @RequestBody InStockPurchaseInStockOrderDTO inStock) {
        log.info("采购入库单入库:inStock{}", inStock);
        return storageApiService.inStockPurchaseInStockOrder(inStock);
    }

    @GetMapping("/purchaseInStockOrderPageQuery")
    public Result<PageResult<PurchaseInStockOrderPageVO>> purchaseInStockOrderPageQuery(PurchaseInStockOrderPageQuery query) {
        log.info("分页查询采购入库单列表:query{}", query);
        return storageApiService.purchaseInStockOrderPageQuery(query);
    }

    @GetMapping("/purchaseInStockOrderDetailQuery")
    public Result<PurchaseInStockOrderDetailVO> purchaseInStockOrderDetailQuery(Long id) {
        log.info("采购入库单详细:id{}", id);
        return storageApiService.purchaseInStockOrderDetailQuery(id);
    }

    // ==================== SalesOutStockOrder 销售出库单管理 ====================

    @PostMapping("/outStockSalesOutStockOrder")
    public Result<Void> outStockSalesOutStockOrder(@Validated @RequestBody OutStockSalesOutStockOrderDTO dto) {
        log.info("销售出库单出库:dto{}", dto);
        return storageApiService.outStockSalesOutStockOrder(dto);
    }

    @GetMapping("/salesOutStockOrderPageQuery")
    public Result<PageResult<SalesOutStockOrderPageVO>> salesOutStockOrderPageQuery(SalesOutStockOrderPageQuery query) {
        log.info("分页查询销售出库单列表:query{}", query);
        return storageApiService.salesOutStockOrderPageQuery(query);
    }

    @GetMapping("/salesOutStockOrderDetailQuery")
    public Result<SalesOutStockOrderDetailVO> salesOutStockOrderDetailQuery(Long id) {
        log.info("销售出库单详细:id{}", id);
        return storageApiService.salesOutStockOrderDetailQuery(id);
    }

    // ==================== Stock 库存管理 ====================

    @GetMapping("/stockPageQuery")
    public Result<PageResult<StockPageQueryVO>> stockPageQuery(StockPageQuery query) {
        log.info("分页查询库存列表:query{}", query);
        return storageApiService.stockPageQuery(query);
    }

    // ==================== StockFlow 库存流水管理 ====================

    @GetMapping("/stockFlowPageQuery")
    public Result<PageResult<StockFlowPageVO>> stockFlowPageQuery(StockFlowPageQuery query) {
        log.info("分页查询库存流水列表:query{}", query);
        return storageApiService.stockFlowPageQuery(query);
    }

    // ==================== StockPreorderPublishLog 预售发布日志管理 ====================

    @PostMapping("/publishStock")
    public Result<Void> publishStock(@Validated @RequestBody PublishStockDTO publishStock) {
        log.info("发布预售库存:publishStock{}", publishStock);
        return storageApiService.publishStock(publishStock);
    }

    @GetMapping("/stockPreorderPublishLogPageQuery")
    public Result<PageResult<StockPreorderPublishLogPageVO>> stockPreorderPublishLogPageQuery(StockPreorderPublishLogPageQuery query) {
        log.info("分页查询库存预售发布日志:query{}", query);
        return storageApiService.stockPreorderPublishLogPageQuery(query);
    }

    // ==================== AfterSalesIntStockOrder 售后入库单管理 ====================

    @PostMapping("/inStock")
    public Result<Void> inStock(@Validated @RequestBody AfterSalesIntStockDTO dto) {
        log.info("销售入库单入库:dto{}", dto);
        return storageApiService.inStock(dto);
    }

    @GetMapping("/afterSalesIntStockOrderPageQuery")
    public Result<PageResult<AfterSalesIntStockOrderPageVO>> afterSalesIntStockOrderPageQuery(PageQuery query) {
        log.info("分页查询售后入库单:query{}", query);
        return storageApiService.afterSalesIntStockOrderPageQuery(query);
    }

    @GetMapping("/afterSalesIntStockOrderDetailQuery")
    public Result<AfterSalesIntStockOrderDetailVO> afterSalesIntStockOrderDetailQuery(Long id) {
        log.info("销售入库单详细:id{}", id);
        return storageApiService.afterSalesIntStockOrderDetailQuery(id);
    }

    // ==================== ReconciliationDiff 对账差异管理 ====================

    @GetMapping("/reconciliationDiffPageQuery")
    public Result<PageResult<ReconciliationDiffPageVO>> reconciliationDiffPageQuery(ReconciliationDiffPageQuery query) {
        log.info("分页查询对账差异列表:query{}", query);
        return storageApiService.reconciliationDiffPageQuery(query);
    }

    // ==================== ReconciliationOrderDetail 库存对账订单详细管理 ====================

    @GetMapping("/reconciliationOrderDetailPageQuery")
    public Result<PageResult<ReconciliationOrderDetailPageVO>> reconciliationOrderDetailPageQuery(ReconciliationOrderDetailPageQuery query) {
        log.info("分页查询库存对账订单详细列表:query{}", query);
        return storageApiService.reconciliationOrderDetailPageQuery(query);
    }
    @GetMapping("/shortStockReconciliationScanTask")
    public Result<Void> shortStockReconciliationScanTask(@RequestParam("batchId") String batchId){
        log.info("手动开启短库存对账任务:batchId{}", batchId);
        return storageApiService.shortStockReconciliationScanTask(batchId);
    }

    @GetMapping("/longStockReconciliationScanTask")
    public Result<Void> longStockReconciliationScanTask(@RequestParam("batchId") String batchId){
        log.info("手动开启长库存对账任务:batchId{}", batchId);
        return storageApiService.longStockReconciliationScanTask(batchId);
    }


}
