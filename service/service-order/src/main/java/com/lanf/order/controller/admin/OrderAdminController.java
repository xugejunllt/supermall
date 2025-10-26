package com.lanf.order.controller.admin;


import com.lanf.mybatis.base.PageResult;
import com.lanf.order.model.dto.DeliveryDTO;
import com.lanf.order.model.query.OrderPageQuery2;
import com.lanf.order.model.vo.OrderDetailVO2;
import com.lanf.order.model.vo.OrderPageVO2;
import com.lanf.order.service.IOrderService;
import com.lanf.order.service.layout.InterfaceLayoutService;
import com.lanf.web.result.Result;
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
    private InterfaceLayoutService interfaceLayoutService;


    @PostMapping("/delivery")
    public Result delivery(@Validated @RequestBody DeliveryDTO dto) {

        log.info("进行发货:{}dto", dto);
        interfaceLayoutService.delivery(dto);
        return Result.ok();
    }

    @GetMapping("/orderPageVO2")
    public Result<PageResult<OrderPageVO2>> orderPageVO2(@Validated OrderPageQuery2 query2) {

        log.info("分页查询订单列表:{}", query2);

        return Result.ok(orderService.orderPageVO2(query2));
    }
    @GetMapping("/orderDetailVO2")
    public Result<OrderDetailVO2> orderDetailVO2(@Validated Long orderId) {

        log.info("查询订单明细:{}", orderId);

        return Result.ok(orderService.orderDetailVO2(orderId));
    }

}

