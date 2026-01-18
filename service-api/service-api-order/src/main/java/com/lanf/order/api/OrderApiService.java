package com.lanf.order.api;

import com.lanf.constant.result.Result;
import com.lanf.order.model.dto.CreateOrderDTO;
import com.lanf.order.model.query.ContrastBillOrderQuery;
import com.lanf.order.model.vo.OrderVO;
import com.lanf.order.model.vo.OrderVO2;
import org.dromara.hmily.annotation.Hmily;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Component
@FeignClient(name = "service-order",url = "localhost:9007") //调用的服务名称
public interface OrderApiService {

    @Hmily
    @PostMapping("/order/api/createOrder")
    public Result<Void> createOrder(@Validated @RequestBody CreateOrderDTO dto);


    @Deprecated
    @PostMapping("/order/orderApi/queryByOrderId")
    public Result<List<OrderVO>> queryByOrderId(@RequestBody List<Long> orderIdList);
    @PostMapping("/order/orderApi/contrastBillOrderCountQuery")
    @Deprecated
    public Result<Integer> contrastBillOrderCountQuery(@RequestBody ContrastBillOrderQuery query);
    @PostMapping("/order/orderApi/contrastBillOrderIdQuery")
    @Deprecated
    public Result<List<Long>> contrastBillOrderIdQuery(@RequestBody ContrastBillOrderQuery query);
    @GetMapping("/order/orderApi/queryById2")
    @Deprecated
    public Result<OrderVO2> queryById2(@RequestParam("id") Long id);
}
