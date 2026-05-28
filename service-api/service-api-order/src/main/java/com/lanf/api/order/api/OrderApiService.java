package com.lanf.api.order.api;

import com.lanf.api.order.model.dto.AllowOutboundDTO;
import com.lanf.api.order.model.dto.CancelOrderApiDTO;
import com.lanf.api.order.model.dto.DeliveryDTO;
import com.lanf.api.order.model.query.ContrastBillOrderQuery;
import com.lanf.api.order.model.query.OrderDetailQuery;
import com.lanf.api.order.model.query.OrderDocumentQuery;
import com.lanf.api.order.model.query.ReconciliationOrderItemQuery;
import com.lanf.api.order.model.vo.OrderDetailForAdminVO;
import com.lanf.api.order.model.vo.OrderDocumentVO;
import com.lanf.api.order.model.vo.OrderVO2;
import com.lanf.api.order.model.vo.ReconciliationOrderItemVO;
import com.lanf.constant.result.Result;
import org.dromara.hmily.annotation.Hmily;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Component
@FeignClient(name = "service-order", url = "localhost:9007") //调用的服务名称
public interface OrderApiService {



    @Hmily
    @PostMapping("/order/api/cancelOrder")
    public Result<Void> cancelOrder(@Validated @RequestBody CancelOrderApiDTO dto);

    /**
     * 查询订单轨迹
     */
    @PostMapping("/order/api/reconciliationOrderItemQuery")
    Result<ReconciliationOrderItemVO> reconciliationOrderItemQuery(@RequestBody ReconciliationOrderItemQuery query);


    @PostMapping("/order/api/orderDocumentQuery")
    public Result<OrderDocumentVO> orderDocumentQuery( @RequestBody OrderDocumentQuery query);


    @PostMapping("/order/orderApi/contrastBillOrderCountQuery")
    @Deprecated
    public Result<Integer> contrastBillOrderCountQuery(@RequestBody ContrastBillOrderQuery query);

    @PostMapping("/order/orderApi/contrastBillOrderIdQuery")
    @Deprecated
    public Result<List<Long>> contrastBillOrderIdQuery(@RequestBody ContrastBillOrderQuery query);

    @GetMapping("/order/orderApi/queryById2")
    @Deprecated
    public Result<OrderVO2> queryById2(@RequestParam("id") Long id);

    // ==================== Admin 订单管理 ====================

    @PostMapping("/order/admin/order/allowOutbound")
    public Result<Void> allowOutbound(@RequestBody AllowOutboundDTO dto);

    @PostMapping("/order/admin/order/delivery")
    public Result<Void> delivery(@RequestBody DeliveryDTO dto);


    @GetMapping("/order/admin/order/orderDetailForAdminQuery")
    public Result<OrderDetailForAdminVO> orderDetailForAdminQuery(@SpringQueryMap OrderDetailQuery query);
}
