package com.lanf.order.controller.aip;

import com.lanf.order.model.query.ContrastBillOrderQuery;
import com.lanf.order.model.vo.OrderVO;
import com.lanf.order.model.vo.OrderVO2;
import com.lanf.order.service.IOrderService;
import com.lanf.constant.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/orderApi")
public class OrderApiController {


    @Autowired
    private IOrderService orderService;

    @PostMapping("/queryByOrderId")
    public Result<List<OrderVO>> queryByOrderId(@RequestBody List<Long> orderIdList) {

        log.info("根据订单ID查询订单信息:orderIdList{}", orderIdList);

        return Result.ok(orderService.queryByOrderId(orderIdList));
    }

    @PostMapping("/contrastBillOrderCountQuery")
    public Result<Integer> contrastBillOrderCountQuery(@RequestBody ContrastBillOrderQuery query) {

        log.info("统计对账订单数量:query{}", query);

        return Result.ok(orderService.contrastBillOrderCountQuery(query));
    }
    @PostMapping("/contrastBillOrderIdQuery")
    public Result<List<Long>> contrastBillOrderIdQuery(@RequestBody ContrastBillOrderQuery query) {

        log.info("统计对账订单id查询:query{}", query);

        return Result.ok(orderService.contrastBillOrderIdQuery(query));
    }

    @GetMapping("/queryById2")
    public Result<OrderVO2> queryById2(@RequestParam("id") Long id) {

        log.info("查询订单信息:id{}", id);

        return Result.ok(orderService.queryById(id));
    }
}
