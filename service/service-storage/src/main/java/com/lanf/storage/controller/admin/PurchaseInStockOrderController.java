package com.lanf.storage.controller.admin;


import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import com.lanf.api.storage.model.dto.InStockPurchaseInStockOrderDTO;
import com.lanf.api.storage.model.query.PurchaseInStockOrderPageQuery;
import com.lanf.api.storage.model.vo.PurchaseInStockOrderDetailVO;
import com.lanf.api.storage.model.vo.PurchaseInStockOrderPageVO;
import com.lanf.storage.service.storage.IPurchaseInStockOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 采购入库单 前端控制器
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-05-30
 */
@Slf4j
@RestController
@RequestMapping("/admin/purchaseInStockOrder")
public class PurchaseInStockOrderController {

    @Autowired
    private IPurchaseInStockOrderService purchaseStorageOrderService;

    /**
     * 采购入库单入库
     *
     *
     */
    @PostMapping("/inStockPurchaseInStockOrder")
    public Result<Void> inStockPurchaseInStockOrder(@Validated @RequestBody InStockPurchaseInStockOrderDTO inStock) {

        log.info("采购入库单 入库:inStock{}", inStock);
        purchaseStorageOrderService.inStockPurchaseInStockOrder(inStock);
        return Result.ok();
    }

    @GetMapping("/purchaseInStockOrderPageQuery")
    public Result<PageResult<PurchaseInStockOrderPageVO>> purchaseInStockOrderPage(PurchaseInStockOrderPageQuery query) {
        log.info("分页查询库采购入库单列表:query{}", query);

        return Result.ok(purchaseStorageOrderService.purchaseInStockOrderPageQuery(query));
    }

    @GetMapping("/purchaseInStockOrderDetailQuery")
    public Result<PurchaseInStockOrderDetailVO> purchaseInStockOrderDetailQuery(Long id) {
        log.info("采购单详细:id{}", id);

        return Result.ok(purchaseStorageOrderService.purchaseInStockOrderDetailQuery(id));
    }

}

