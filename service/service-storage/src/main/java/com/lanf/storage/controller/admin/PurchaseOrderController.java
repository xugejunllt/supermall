package com.lanf.storage.controller.admin;


import com.lanf.api.storage.model.dto.AddPurchaseOrderDTO;
import com.lanf.api.storage.model.dto.ReviewDTO;
import com.lanf.api.storage.model.query.PurchaseOrderPageQuery;
import com.lanf.api.storage.model.vo.PurchaseOrderDetailVO;
import com.lanf.api.storage.model.vo.PurchaseOrderPageVO;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import com.lanf.storage.service.purchase.IPurchaseOrderService;
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

    @PostMapping("/addPurchaseOrder")
    public Result<Void> addPurchaseOrder(@Validated @RequestBody AddPurchaseOrderDTO purchaseOrderAdd) {

        log.info("添加采购单:purchaseOrderAdd{}", purchaseOrderAdd);
        purchaseOrderService.addPurchaseOrder(purchaseOrderAdd);
        return Result.ok();
    }


    @GetMapping("/purchaseOrderPageQuery")
    public Result<PageResult<PurchaseOrderPageVO>> purchaseOrderPageQuery(PurchaseOrderPageQuery query) {

        log.info("分页查询采购单列表:query{}", query);

        return Result.ok(purchaseOrderService.purchaseOrderPageQuery(query));
    }

    @GetMapping("/purchaseOrderDetailQuery")
    public Result<PurchaseOrderDetailVO> purchaseOrderDetailQuery(Long id) {

        log.info("采购单详细:id{}", id);

        return Result.ok(purchaseOrderService.purchaseOrderDetailQuery(id));
    }

    /**
     * 采购单审核
     */
    @PostMapping("/auditApprove")
    public Result<Void> auditApprove(@RequestBody ReviewDTO dto) {

        log.info("采购单审核通过:dto{},", dto);
        purchaseOrderService.auditApprove(dto.getId());
        return Result.ok();
    }

}

