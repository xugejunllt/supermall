package com.lanf.system.controller.order;

import com.lanf.api.order.api.OrderApiService;
import com.lanf.api.order.model.dto.AddExpressDTO;
import com.lanf.api.order.model.dto.AllowOutboundDTO;
import com.lanf.api.order.model.dto.BusinessAgreeDTO;
import com.lanf.api.order.model.dto.BusinessReceiverDTO;
import com.lanf.api.order.model.dto.CompleteRefundDTO;
import com.lanf.api.order.model.dto.DeliveryDTO;
import com.lanf.api.order.model.query.OrderDetailQuery;
import com.lanf.api.order.model.vo.AfterSalesOrderForUserDetailVO;
import com.lanf.api.order.model.vo.AfterSalesOrderForUserPageVO;
import com.lanf.api.order.model.vo.ExpressPageVO;
import com.lanf.api.order.model.vo.OrderDetailForAdminVO;
import com.lanf.constant.model.query.PageQuery;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderAdminController {

    @Autowired
    private OrderApiService orderApiService;

    @PostMapping("/allowOutbound")
    public Result<Void> allowOutbound(@Validated @RequestBody AllowOutboundDTO dto) {
        log.info("允许发货:{}dto", dto);
        return orderApiService.allowOutbound(dto);
    }

    @PostMapping("/delivery")
    public Result<Void> delivery(@Validated @RequestBody DeliveryDTO dto) {
        log.info("进行发货:{}dto", dto);
        return orderApiService.delivery(dto);
    }

    @GetMapping("/orderDetailForAdminQuery")
    public Result<OrderDetailForAdminVO> orderDetailForAdminQuery(@Validated OrderDetailQuery query) {
        log.info("admin查询订单详细:{}", query);
        return orderApiService.orderDetailForAdminQuery(query);
    }
    @PostMapping("/addExpress")
    public Result<Void> addExpress(@Validated @RequestBody AddExpressDTO dto) {
        log.info("添加快递公司:{}", dto);
        orderApiService.addExpress(dto);
        return Result.ok();
    }

    @GetMapping("/expressPageQuery")
    public Result<PageResult<ExpressPageVO>> expressPageQuery(@Validated PageQuery query){

        log.info("分页查询快递公司");
        return orderApiService.expressPageQuery(query);
    }

    @GetMapping("/afterSalesOrderForUserPageQuery")
    public Result<PageResult<AfterSalesOrderForUserPageVO>> afterSalesOrderForUserPageQuery(@Validated PageQuery query) {
        log.info("分页查询售后单:query{}", query);
        return orderApiService.afterSalesOrderForUserPageQuery(query);
    }

    @GetMapping("/afterSalesOrderForUserDetailQuery")
    public Result<AfterSalesOrderForUserDetailVO> afterSalesOrderForUserDetailQuery(@RequestParam("id") Long id) {
        log.info("售后单详细:query{}", id);
        return orderApiService.afterSalesOrderForUserDetailQuery(id);
    }

    @PostMapping("/businessAgree")
    public Result<Void> businessAgree(@RequestBody @Validated BusinessAgreeDTO dto) {
        log.info("商家同意:dto{}", dto);
        return orderApiService.businessAgree(dto);
    }

    @PostMapping("/businessReceiver")
    public Result<Void> businessReceiver(@RequestBody @Validated BusinessReceiverDTO dto) {
        log.info("商家签收商品:dto{}", dto);
        return orderApiService.businessReceiver(dto);
    }

    @PostMapping("/completeRefund")
    public Result<Void> completeRefund(@RequestBody @Validated CompleteRefundDTO dto) {
        log.info("发起退款:dto{}", dto);
        return orderApiService.completeRefund(dto);
    }
    @GetMapping("/orderAutoCloseScanTask")
    public Result<Void> orderAutoCloseScanTask(){
        log.info("手动开启自动关闭订单定时任务");
        return orderApiService.orderAutoCloseScanTask();
    }

}
