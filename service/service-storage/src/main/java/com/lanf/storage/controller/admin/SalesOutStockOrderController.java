package com.lanf.storage.controller.admin;


import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import com.lanf.api.storage.model.dto.OutStockSalesOutStockOrderDTO;
import com.lanf.api.storage.model.query.SalesOutStockOrderPageQuery;
import com.lanf.api.storage.model.vo.SalesOutStockOrderDetailVO;
import com.lanf.api.storage.model.vo.SalesOutStockOrderPageVO;
import com.lanf.storage.service.storage.ISalesOutStockOrderService;
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
@RequestMapping("/admin/salesOutStockOrder")
public class SalesOutStockOrderController {

    @Autowired
    private ISalesOutStockOrderService salesOutStockOrderService;

    @PostMapping("/outStockSalesOutStockOrder")
    public Result<Void> outStockSalesOutStockOrder(@Validated @RequestBody OutStockSalesOutStockOrderDTO dto) {

        log.info("销售出库单出库:dto{}", dto);
        salesOutStockOrderService.outStockSalesOutStockOrder(dto);
        return Result.ok();
    }

    @GetMapping("/salesOutStockOrderPageQuery")
    public Result<PageResult<SalesOutStockOrderPageVO>> salesOutStockOrderPageQuery(SalesOutStockOrderPageQuery query) {

        log.info("分页查询库销售出库单列表:query{}", query);

        return Result.ok(salesOutStockOrderService.salesOutStockOrderPageQuery(query));
    }

    @GetMapping("/salesOutStockOrderDetail")
    public Result<SalesOutStockOrderDetailVO> salesOutStockOrderDetail(Long id) {

        log.info("出库单详细:id{}", id);

        return Result.ok(salesOutStockOrderService.salesOutStockOrderDetail(id));
    }

}

