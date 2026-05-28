package com.lanf.storage.controller.admin;


import com.lanf.api.storage.model.dto.AfterSalesIntStockDTO;
import com.lanf.api.storage.model.vo.AfterSalesIntStockOrderDetailVO;
import com.lanf.api.storage.model.vo.AfterSalesIntStockOrderPageVO;
import com.lanf.constant.model.query.PageQuery;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import com.lanf.storage.service.storage.IAfterSalesIntStockOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 销售出库单 前端控制器
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-09
 */
@Slf4j
@RestController
@RequestMapping("/admin/afterSalesIntStockOrder")
public class AfterSalesIntStockOrderController {


    @Autowired
    private IAfterSalesIntStockOrderService afterSalesIntStockOrderService;

    @PostMapping("/inStockAfterSalesIntStockOrder")
    public Result<Void> inStock(@Validated @RequestBody AfterSalesIntStockDTO dto) {

        log.info("销售入库单入库:dto{}", dto);
        afterSalesIntStockOrderService.inStock(dto);
        return Result.ok();
    }

    @GetMapping("/afterSalesIntStockOrderPageQuery")
    public Result<PageResult<AfterSalesIntStockOrderPageVO>> afterSalesIntStockOrderPageQuery(@Validated PageQuery query) {

        log.info("分页查询售后入库单:query{}", query);

        return Result.ok(afterSalesIntStockOrderService.afterSalesIntStockOrderPageQuery(query));
    }

    @GetMapping("/afterSalesIntStockOrderDetailQuery")
    public Result<AfterSalesIntStockOrderDetailVO> afterSalesIntStockOrderDetailQuery(Long id) {

        log.info("销售入库单详细:id:{}", id);

        return Result.ok(afterSalesIntStockOrderService.afterSalesIntStockOrderDetailQuery(id));
    }

}

