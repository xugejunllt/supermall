package com.lanf.api.storage.api;

import com.lanf.api.storage.model.dto.*;
import com.lanf.api.storage.model.query.*;
import com.lanf.api.storage.model.vo.*;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Component
@FeignClient(name = "service-storage",url = "localhost:9004")
public interface StorageApiService {



    @PostMapping("/storage/storageApi/querySkuCodeList")
    Result< List<StockVO>> querySkuCodeList(@RequestBody List<String> skuCodeList);

    // ==================== Warehouse 仓库管理 ====================

    @GetMapping("/storage/admin/warehouse/warehousePageQuery")
    Result<PageResult<WarehousePageVO>> warehousePageQuery(@SpringQueryMap WarehousePageQuery query);

    @PostMapping("/storage/admin/warehouse/addWarehouse")
    Result<Void> addWarehouse(@RequestBody AddWarehouseDTO warehouse);

    // ==================== Supplier 供应商管理 ====================

    @GetMapping("/storage/admin/supplier/supplierPageQuery")
    Result<PageResult<SupplierPageVO>> supplierPageQuery(@SpringQueryMap SupplierPageQuery query);

    @PostMapping("/storage/admin/supplier/addSupplier")
    Result<Void> addSupplier(@RequestBody AddSupplierDTO supplier);

    @GetMapping("/storage/admin/supplier/supplierListQuery")
    Result<List<SupplierListVO>> supplierList();

    // ==================== PurchaseOrder 采购单管理 ====================

    @PostMapping("/storage/admin/purchaseOrder/addPurchaseOrder")
    Result<Void> addPurchaseOrder(@RequestBody AddPurchaseOrderDTO purchaseOrderAdd);

    @GetMapping("/storage/admin/purchaseOrder/purchaseOrderPageQuery")
    Result<PageResult<PurchaseOrderPageVO>> purchaseOrderPageQuery(@SpringQueryMap PurchaseOrderPageQuery query);

    @GetMapping("/storage/admin/purchaseOrder/purchaseOrderDetailQuery")
    Result<PurchaseOrderDetailVO> purchaseOrderDetailQuery(@RequestParam("id") Long id);

    @PostMapping("/storage/admin/purchaseOrder/auditApprove")
    Result<Void> auditApprove(@RequestBody ReviewDTO dto);

    // ==================== PurchaseInStockOrder 采购入库单管理 ====================

    @PostMapping("/storage/admin/purchaseInStockOrder/inStockPurchaseInStockOrder")
    Result<Void> inStockPurchaseInStockOrder(@RequestBody InStockPurchaseInStockOrderDTO inStock);

    @GetMapping("/storage/admin/purchaseInStockOrder/purchaseInStockOrderPageQuery")
    Result<PageResult<PurchaseInStockOrderPageVO>> purchaseInStockOrderPageQuery(@SpringQueryMap PurchaseInStockOrderPageQuery query);

    @GetMapping("/storage/admin/purchaseInStockOrder/purchaseInStockOrderDetailQuery")
    Result<PurchaseInStockOrderDetailVO> purchaseInStockOrderDetailQuery(@RequestParam("id")Long id);

    // ==================== SalesOutStockOrder 销售出库单管理 ====================

    @PostMapping("/storage/admin/salesOutStockOrder/outStockSalesOutStockOrder")
    Result<Void> outStockSalesOutStockOrder(@RequestBody OutStockSalesOutStockOrderDTO dto);

    @GetMapping("/storage/admin/salesOutStockOrder/salesOutStockOrderPageQuery")
    Result<PageResult<SalesOutStockOrderPageVO>> salesOutStockOrderPageQuery(@SpringQueryMap SalesOutStockOrderPageQuery query);

    @GetMapping("/storage/admin/salesOutStockOrder/salesOutStockOrderDetail")
    Result<SalesOutStockOrderDetailVO> salesOutStockOrderDetail(@RequestParam("id")Long id);

    // ==================== Stock 库存管理 ====================

    @GetMapping("/storage/admin/stock/stockPageQuery")
    Result<PageResult<StockPageQueryVO>> stockPageQuery(@SpringQueryMap StockPageQuery query);

    // ==================== StockFlow 库存流水管理 ====================

    @GetMapping("/storage/admin/stockFlow/stockFlowPageQuery")
    Result<PageResult<StockFlowPageVO>> stockFlowPageQuery(@SpringQueryMap StockFlowPageQuery query);

    // ==================== StockPreorderPublishLog 预售发布日志管理 ====================

    @PostMapping("/storage/admin/stockPreorderPublishLog/publishStock")
    Result<Void> publishStock(@RequestBody PublishStockDTO publishStock);

}
