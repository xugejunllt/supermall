package com.lanf.order.api;

import com.lanf.order.model.query.ContrastBillOrderQuery;
import com.lanf.order.model.vo.OrderVO;
import com.lanf.order.model.vo.OrderVO2;
import com.lanf.constant.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Component
@FeignClient(name = "service-order",url = "localhost:9007") //调用的服务名称
public interface OrderApiService {

    @PostMapping("/order/orderApi/queryByOrderId")
    public Result<List<OrderVO>> queryByOrderId(@RequestBody List<Long> orderIdList);
    @PostMapping("/order/orderApi/contrastBillOrderCountQuery")
    public Result<Integer> contrastBillOrderCountQuery(@RequestBody ContrastBillOrderQuery query);
    @PostMapping("/order/orderApi/contrastBillOrderIdQuery")
    public Result<List<Long>> contrastBillOrderIdQuery(@RequestBody ContrastBillOrderQuery query);
    @GetMapping("/order/orderApi/queryById2")
    public Result<OrderVO2> queryById2(@RequestParam("id") Long id);
}
