package com.lanf.storage.controller.admin;


import com.lanf.constant.web.PageResult;
import com.lanf.storage.model.dto.CalculatePurchaseOrderMoneyDTO;
import com.lanf.storage.model.dto.PurchaseOrderAddDTO;
import com.lanf.storage.model.dto.ReviewDTO;
import com.lanf.storage.model.query.PurchaseOrderPageQuery;
import com.lanf.storage.model.vo.CalculatePurchaseOrderMoneyVO;
import com.lanf.storage.model.vo.PurchaseOrderDetailVO;
import com.lanf.storage.model.vo.PurchaseOrderPageVO;
import com.lanf.storage.service.purchase.IPurchaseOrderService;
import com.lanf.constant.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 采购单 前端控制器
 * </p>
 *
 * @author
 * @since 2024-05-30
 */
@Slf4j
@RestController
@RequestMapping("/admin/purchaseOrder")
public class PurchaseOrderController {

    @Autowired
    private IPurchaseOrderService purchaseOrderService;

    @PostMapping("/purchaseOrderAdd")
    public Result purchaseOrderAdd(@Validated @RequestBody PurchaseOrderAddDTO purchaseOrderAdd) {
        log.info("添加采购单:purchaseOrderAdd{}", purchaseOrderAdd);
        purchaseOrderService.purchaseOrderAdd(purchaseOrderAdd);
        return Result.ok();
    }

    @PostMapping("/calculatePurchaseOrderMoney")
    public Result<CalculatePurchaseOrderMoneyVO> calculatePurchaseOrderMoney(@Validated @RequestBody CalculatePurchaseOrderMoneyDTO calculatePurchaseOrderMoney) {

        log.info("计算采购单各项金额:purchaseOrderAdd{}", calculatePurchaseOrderMoney);
        return Result.ok(purchaseOrderService.calculatePurchaseOrderMoney(calculatePurchaseOrderMoney));
    }

    @GetMapping("/purchaseOrderPage")
    public Result<PageResult<PurchaseOrderPageVO>> purchaseOrderPage(PurchaseOrderPageQuery query) {

        log.info("分页查询采购单列表:query{}", query);

        return Result.ok(purchaseOrderService.purchaseOrderPage(query));
    }

    @GetMapping("/purchaseOrderDetail")
    public Result<PurchaseOrderDetailVO> purchaseOrderDetail(Long id) {

        log.info("采购单详细:id{}", id);

        return Result.ok(purchaseOrderService.purchaseOrderDetail(id));
    }

    /**
     * 采购单审核
     */
    @PostMapping("/auditApprove")
    public Result auditApprove(@RequestBody ReviewDTO dto) {

        log.info("采购单审核通过:dto{},", dto);
        purchaseOrderService.auditApprove(dto.getId());
        return Result.ok();
    }

}

