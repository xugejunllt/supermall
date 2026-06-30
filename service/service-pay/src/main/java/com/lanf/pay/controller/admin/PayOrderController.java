package com.lanf.pay.controller.admin;

import com.lanf.api.pay.model.query.*;
import com.lanf.api.pay.model.vo.*;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import com.lanf.pay.service.pay.*;
import com.lanf.pay.service.trade.ITradeOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/admin/payOrder")
public class PayOrderController {

    @Autowired
    private ITradeOrderService tradeOrderService;

    @Autowired
    private IPayOrderFlowService payOrderFlowService;

    @Autowired
    private IRefundOrderService refundOrderService;

    @Autowired
    private IRefundOrderFlowService refundOrderFlowService;

    @Autowired
    private ITransferOrderService transferOrderService;

    @Autowired
    private ITransferOrderFlowService transferOrderFlowService;

    /**
     * 分页查询交易订单
     */
    @GetMapping("/tradeOrderPageQuery")
    public Result<PageResult<TradeOrderPageVO>> tradeOrderPageQuery(@Validated TradeOrderPageQuery query) {
        log.info("分页查询交易订单: {}", query);
        return Result.ok(tradeOrderService.tradeOrderPageQuery(query));
    }

    /**
     * 分页查询支付流水
     */
    @GetMapping("/payOrderFlowPageQuery")
    public Result<PageResult<PayOrderFlowPageVO>> payOrderFlowPageQuery(@Validated PayOrderFlowPageQuery query) {
        log.info("分页查询支付流水: {}", query);
        return Result.ok(payOrderFlowService.payOrderFlowPageQuery(query));
    }

    /**
     * 分页查询退款单
     */
    @GetMapping("/refundOrderPageQuery")
    public Result<PageResult<RefundOrderPageVO>> refundOrderPageQuery(@Validated RefundOrderPageQuery query) {
        log.info("分页查询退款单: {}", query);
        return Result.ok(refundOrderService.refundOrderPageQuery(query));
    }

    /**
     * 分页查询退款单流水
     */
    @GetMapping("/refundOrderFlowPageQuery")
    public Result<PageResult<RefundOrderFlowPageVO>> refundOrderFlowPageQuery(@Validated RefundOrderFlowPageQuery query) {
        log.info("分页查询退款单流水: {}", query);
        return Result.ok(refundOrderFlowService.refundOrderFlowPageQuery(query));
    }

    /**
     * 分页查询转账单
     */
    @GetMapping("/transferOrderPageQuery")
    public Result<PageResult<TransferOrderPageVO>> transferOrderPageQuery(@Validated TransferOrderPageQuery query) {
        log.info("分页查询转账单: {}", query);
        return Result.ok(transferOrderService.transferOrderPageQuery(query));
    }

    /**
     * 分页查询转账单流水
     */
    @GetMapping("/transferOrderFlowPageQuery")
    public Result<PageResult<TransferOrderFlowPageVO>> transferOrderFlowPageQuery(@Validated TransferOrderFlowPageQuery query) {
        log.info("分页查询转账单流水: {}", query);
        return Result.ok(transferOrderFlowService.transferOrderFlowPageQuery(query));
    }

}
