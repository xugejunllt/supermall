package com.lanf.api.order.api;

import com.lanf.api.order.model.dto.*;
import com.lanf.api.order.model.query.OrderDetailQuery;
import com.lanf.api.order.model.query.OrderDocumentQuery;
import com.lanf.api.order.model.query.ReconciliationOrderItemQuery;
import com.lanf.api.order.model.vo.*;
import com.lanf.constant.model.query.PageQuery;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Component
@FeignClient(name = "service-order") //调用的服务名称
public interface OrderApiService {



    /**
     * 查询订单轨迹
     */
    @PostMapping("/order/api/reconciliationOrderItemQuery")
    Result<ReconciliationOrderItemVO> reconciliationOrderItemQuery(@RequestBody ReconciliationOrderItemQuery query);


    @PostMapping("/order/api/orderDocumentQuery")
    public Result<OrderDocumentVO> orderDocumentQuery( @RequestBody OrderDocumentQuery query);


    // ==================== Admin 订单管理 ====================

    @PostMapping("/order/admin/order/allowOutbound")
    public Result<Void> allowOutbound(@RequestBody AllowOutboundDTO dto);

    @PostMapping("/order/admin/order/delivery")
    public Result<Void> delivery(@RequestBody DeliveryDTO dto);


    @GetMapping("/order/admin/order/orderDetailForAdminQuery")
    public Result<OrderDetailForAdminVO> orderDetailForAdminQuery(@SpringQueryMap OrderDetailQuery query);

    // ==================== Admin 快递管理 ====================

    @PostMapping("/order/admin/express/addExpress")
    public Result<Void> addExpress( @RequestBody AddExpressDTO dto);

    @GetMapping("/order/admin/express/expressPageQuery")
    public Result<PageResult<ExpressPageVO>> expressPageQuery(@SpringQueryMap PageQuery query);

    // ==================== Admin 售后管理 ====================

    @GetMapping("/order/admin/afterSalesOrder/afterSalesOrderForUserPageQuery")
    public Result<PageResult<AfterSalesOrderForUserPageVO>> afterSalesOrderForUserPageQuery(@SpringQueryMap PageQuery query);

    @GetMapping("/order/admin/afterSalesOrder/afterSalesOrderForUserDetailQuery")
    public Result<AfterSalesOrderForUserDetailVO> afterSalesOrderForUserDetailQuery(@RequestParam("id") Long id);


    @PostMapping("/order/admin/afterSalesOrder/businessAgree")
    public Result<Void> businessAgree(@RequestBody  BusinessAgreeDTO dto);

    @PostMapping("/order/admin/afterSalesOrder/businessReceiver")
    public Result<Void> businessReceiver(@RequestBody  BusinessReceiverDTO dto);

    @PostMapping("/order/admin/afterSalesOrder/completeRefund")
    public Result<Void> completeRefund(@RequestBody  CompleteRefundDTO dto);

    @GetMapping("/order/admin/order/orderAutoCloseScanTask")
    public Result<Void> orderAutoCloseScanTask();

}
