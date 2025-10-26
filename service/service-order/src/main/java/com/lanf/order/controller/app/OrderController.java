package com.lanf.order.controller.app;


import com.lanf.common.utils.BigDecimalUtils;
import com.lanf.mybatis.base.PageQuery;
import com.lanf.mybatis.base.PageResult;
import com.lanf.order.model.dto.CancelOrderDTO;
import com.lanf.order.model.dto.DeliveryDTO;
import com.lanf.order.model.dto.SignForDTO;
import com.lanf.order.model.query.OrderPageQuery;
import com.lanf.order.model.vo.OrderDetailVO;
import com.lanf.order.model.vo.OrderPageVO;
import com.lanf.order.service.IOrderService;
import com.lanf.order.service.layout.InterfaceLayoutService;
import com.lanf.order.task.PromiseStatusCheckTask;
import com.lanf.web.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 订单表 前端控制器
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-13
 */
@Slf4j
@RestController
@RequestMapping("/app/order")
public class OrderController {

    @Autowired
    private IOrderService orderService;
    @Autowired
    private InterfaceLayoutService interfaceLayoutService;
    @Autowired
    private PromiseStatusCheckTask promiseStatusCheckTask;


    @GetMapping("/getOrderNumber")
    public Result<String> getOrderNumber(@Validated PageQuery query) {

        log.info("生成订单编号");
        return Result.ok(orderService.getOrderNumber());
    }



    @PostMapping("/signFor")
    public Result signFor(@Validated @RequestBody SignForDTO dto) {

        log.info("签收:{}dto", dto);
        orderService.signFor(dto);
        return Result.ok();
    }


    @GetMapping("/orderPage")
    public Result<PageResult<OrderPageVO>> orderPage(@Validated OrderPageQuery query) {

        log.info("分页查询订单列表:query{}", query);

        return Result.ok(orderService.orderPage(query));
    }

    @GetMapping("/orderDetail")
    public Result<OrderDetailVO> orderDetail(Long id) {

        log.info("查询订单详细:id{}", id);

        return Result.ok(orderService.orderDetail(id));
    }
    @PostMapping("/cancelOrder")
    public Result orderDetail(@Validated @RequestBody CancelOrderDTO dto) {
        log.info("取消订单:dto{}", dto);
        orderService.cancelOrder(dto.getOrderId());
        return Result.ok();

    }
    @GetMapping("/promiseOrderLiquidationTask")
    public Result promiseOrderLiquidationTask() {

        log.info("手动触发定时任务");
        promiseStatusCheckTask.promiseOrderLiquidationTask();
        return Result.ok();

    }
}

