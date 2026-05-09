package com.lanf.order.controller.aip;

import com.lanf.constant.result.Result;
import com.lanf.order.model.dto.BathCreateOrderDTO;
import com.lanf.order.model.dto.CreateOrderDTO;
import com.lanf.order.model.query.OrderDocumentQuery;
import com.lanf.order.model.query.ReconciliationOrderItemQuery;
import com.lanf.order.model.vo.OrderDocumentVO;
import com.lanf.order.model.vo.ReconciliationOrderItemVO;
import com.lanf.order.service.IMainOrderService;
import com.lanf.order.service.IOrderItemService;
import com.lanf.order.service.IOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
public class OrderApiController {


    @Autowired
    private IOrderService orderService;
    @Autowired
    private IMainOrderService mainOrderService;
    @Autowired
    private IOrderItemService orderItemService;


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
    @PostMapping("/reconciliationOrderItemQuery")
    public Result<ReconciliationOrderItemVO> cancelOrder(@Validated @RequestBody ReconciliationOrderItemQuery query) {

        log.info("查询订单轨迹信息{}", query);

        return Result.ok( orderItemService.reconciliationOrderItemQuery(query));
    }

    @PostMapping("/orderDocumentQuery")
    public Result<OrderDocumentVO> orderDocumentQuery(@Validated @RequestBody OrderDocumentQuery query) {

        log.info("查询订单索引信息{}", query);

        return Result.ok( orderService.orderDocumentQuery(query));
    }



}
