package com.lanf.order.controller.admin;


import com.lanf.api.order.model.dto.AllowOutboundDTO;
import com.lanf.api.order.model.dto.DeliveryDTO;
import com.lanf.api.order.model.query.AdminOrderSearchQuery;
import com.lanf.api.order.model.query.OrderDetailQuery;
import com.lanf.api.order.model.vo.AdminOrderListVO;
import com.lanf.api.order.model.vo.OrderDetailForAdminVO;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import com.lanf.order.service.order.IOrderService;
import com.lanf.order.task.OrderAutoCloseTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping("/admin/order")
public class OrderAdminController {

    @Autowired
    private IOrderService orderService;
    @Autowired
    private OrderAutoCloseTask orderAutoCloseTask;


    @PostMapping("/allowOutbound")
    public Result<Void> allowOutbound(@Validated @RequestBody AllowOutboundDTO dto) {

        log.info("允许发货:{}dto", dto);
        orderService.allowOutbound(dto);
        return Result.ok();
    }

    @PostMapping("/delivery")
    public Result<Void> delivery(@Validated @RequestBody DeliveryDTO dto) {

        log.info("进行发货:{}dto", dto);
        orderService.delivery(dto);
        return Result.ok();
    }

    @PostMapping("/orderSearchQuery")
    public Result<PageResult<AdminOrderListVO>> orderSearchQuery(@Validated @RequestBody AdminOrderSearchQuery query) {

        log.info("分页查询订单列表:{}", query);

        return Result.ok(orderService.orderSearchQuery(query));
    }


    @GetMapping("/orderDetailForAdminQuery")
    public Result<OrderDetailForAdminVO> orderDetailForAdminQuery(@Validated OrderDetailQuery query) {

        log.info("admin查询订单详细:{}", query);

        return Result.ok(orderService.orderDetailForAdminQuery(query));
    }

    @GetMapping("/orderAutoCloseScanTask")
    public Result<Void> orderAutoCloseScanTask() {
        log.info("手动开启自动关闭订单定时任务");
        orderAutoCloseTask.orderAutoCloseScanTask();
        return Result.ok();
    }


}

