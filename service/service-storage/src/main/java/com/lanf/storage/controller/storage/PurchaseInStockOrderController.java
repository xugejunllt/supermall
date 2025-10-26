package com.lanf.storage.controller.storage;


import com.lanf.mybatis.base.PageResult;
import com.lanf.storage.model.dto.InStockDTO;
import com.lanf.storage.model.query.PurchaseInStockOrderPageQuery;
import com.lanf.storage.model.vo.PurchaseInStockOrderDetailVO;
import com.lanf.storage.model.vo.PurchaseInStockOrderPageVO;
import com.lanf.storage.service.storage.IPurchaseInStockOrderService;
import com.lanf.web.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/purchaseInStockOrder")
public class PurchaseInStockOrderController {

    @Autowired
    private IPurchaseInStockOrderService purchaseStorageOrderService;

    @PostMapping("/inStock")
    public Result inStock(@Validated @RequestBody InStockDTO inStock) {

        log.info("入库:inStock{}", inStock);
        purchaseStorageOrderService.inStock(inStock);
        return Result.ok();
    }

    @GetMapping("/purchaseInStockOrderPage")
    public Result<PageResult<PurchaseInStockOrderPageVO>> purchaseInStockOrderPage(PurchaseInStockOrderPageQuery query) {
        log.info("分页查询库采购入库单列表:query{}", query);

        return Result.ok(purchaseStorageOrderService.purchaseInStockOrderPage(query));
    }

    @GetMapping("/purchaseInStockOrderDetail")
    public Result<PurchaseInStockOrderDetailVO> purchaseInStockOrderDetail(Long id) {
        log.info("采购单详细:id{}", id);

        return Result.ok(purchaseStorageOrderService.purchaseInStockOrderDetail(id));
    }

}

