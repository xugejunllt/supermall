package com.lanf.order.controller.aip;

import com.lanf.order.model.dto.BathCreateOrderDTO;
import com.lanf.order.model.dto.CancelOrderApiDTO;
import com.lanf.order.model.dto.CreateOrderDTO;
import com.lanf.order.model.query.ContrastBillOrderQuery;
import com.lanf.order.model.vo.OrderVO;
import com.lanf.order.model.vo.OrderVO2;
import com.lanf.order.service.IMainOrderService;
import com.lanf.order.service.IOrderService;
import com.lanf.constant.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
public class OrderApiController {


    @Autowired
    private IOrderService orderService;
    @Autowired
    private IMainOrderService mainOrderService;

    @PostMapping("/createOrder")
    public Result<Void> createOrder(@Validated @RequestBody CreateOrderDTO dto) {

        log.info("创建一笔订单:dto{}", dto);
        orderService.createOrder(dto);
        return Result.ok();
    }

    @PostMapping("/bathCreateOrder")
    public Result<Void> bathCreateOrder(@Validated @RequestBody BathCreateOrderDTO dto) {

        log.info("批量创建订单:dto{}", dto);
        mainOrderService.bathCreateOrder(dto);
        return Result.ok();
    }
    @PostMapping("/cancelOrder")
    public Result<Void> cancelOrder(@Validated @RequestBody CancelOrderApiDTO dto) {

        log.info("取消订单:dto{}", dto);
        orderService.cancelOrder(dto);
        return Result.ok();
    }



    @Deprecated
    @PostMapping("/queryByOrderId")
    public Result<List<OrderVO>> queryByOrderId(@RequestBody List<Long> orderIdList) {

        log.info("根据订单ID查询订单信息:orderIdList{}", orderIdList);

        return Result.ok(orderService.queryByOrderId(orderIdList));
    }
    @Deprecated
    @PostMapping("/contrastBillOrderCountQuery")
    public Result<Integer> contrastBillOrderCountQuery(@RequestBody ContrastBillOrderQuery query) {

        log.info("统计对账订单数量:query{}", query);

        return Result.ok(orderService.contrastBillOrderCountQuery(query));
    }
    @Deprecated
    @PostMapping("/contrastBillOrderIdQuery")
    public Result<List<Long>> contrastBillOrderIdQuery(@RequestBody ContrastBillOrderQuery query) {

        log.info("统计对账订单id查询:query{}", query);

        return Result.ok(orderService.contrastBillOrderIdQuery(query));
    }
    @Deprecated
    @GetMapping("/queryById2")
    public Result<OrderVO2> queryById2(@RequestParam("id") Long id) {

        log.info("查询订单信息:id{}", id);

        return Result.ok(orderService.queryById(id));
    }
}
