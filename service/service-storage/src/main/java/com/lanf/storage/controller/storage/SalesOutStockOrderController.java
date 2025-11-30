package com.lanf.storage.controller.storage;


import com.lanf.mybatis.base.PageResult;
import com.lanf.storage.model.dto.OutStockDTO;
import com.lanf.storage.model.query.SalesOutStockOrderPageQuery;
import com.lanf.storage.model.vo.SalesOutStockOrderDetailVO;
import com.lanf.storage.model.vo.SalesOutStockOrderPageVO;
import com.lanf.storage.service.storage.ISalesOutStockOrderService;
import com.lanf.constant.result.Result;
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
@RequestMapping("/salesOutStockOrder")
public class SalesOutStockOrderController {

    @Autowired
    private ISalesOutStockOrderService salesOutStockOrderService;

    @PostMapping("/outStock")
    public Result outStock(@Validated @RequestBody OutStockDTO dto) {

        log.info("出库:dto{}", dto);
        salesOutStockOrderService.outStock(dto);
        return Result.ok();
    }

    @GetMapping("/salesOutStockOrderPage")
    public Result<PageResult<SalesOutStockOrderPageVO>> salesOutStockOrderPage(SalesOutStockOrderPageQuery query) {

        log.info("分页查询库销售出库单列表:query{}", query);

        return Result.ok(salesOutStockOrderService.salesOutStockOrderPage(query));
    }

    @GetMapping("/salesOutStockOrderDetail")
    public Result<SalesOutStockOrderDetailVO> salesOutStockOrderDetail(Long id) {

        log.info("出库单详细:id{}", id);

        return Result.ok(salesOutStockOrderService.salesOutStockOrderDetail(id));
    }

}

