package com.lanf.api.pay.api;

import com.lanf.api.pay.model.dto.AddPayAccountDTO;
import com.lanf.api.pay.model.dto.CreateMergeTradeOrderDTO;
import com.lanf.api.pay.model.dto.CreateTradeOrderDTO;
import com.lanf.api.pay.model.query.*;
import com.lanf.api.pay.model.vo.*;
import com.lanf.constant.model.vo.PageResult;
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

import java.math.BigDecimal;
import java.util.List;

@Component
@FeignClient(name = "service-pay")
public interface PayApiService {

    @Hmily
    @PostMapping("/pay/api/createPayOrder")
    public Result<Void> createPayOrder(@RequestBody CreateTradeOrderDTO dto);

    @Hmily
    @PostMapping("/pay/api/createMergeTradeOrder")
    public Result<CreateMergeTradeOrderVO> createMergeTradeOrder(@RequestBody CreateMergeTradeOrderDTO dto);

    @PostMapping("/pay/admin/payAccount/addPayAccount")
    public Result<Void> addPayAccount(@Validated @RequestBody AddPayAccountDTO dto);


    @GetMapping("/pay/admin/payAccount/payAccountPageQuery")
    public Result<PageResult<PayAccountPageVO>> payAccountPageQuery(@SpringQueryMap PayAccountPageQuery query);

    // ==================== PaymentSummaryController ====================

    @GetMapping("/pay/admin/paymentSummary/paymentSummaryQuery")
    public Result<PaymentSummarySumVO> paymentSummaryQuery(@SpringQueryMap PaymentSummaryQuery query);

    @GetMapping("/pay/admin/paymentSummary/clearingDetailPageQuery")
    public Result<PageResult<ClearingDetailPageVO>> clearingDetailPageQuery(@SpringQueryMap ClearingDetailPageQuery query);

    @PostMapping("/pay/admin/paymentSummary/sumIncomeMoney")
    public Result<BigDecimal> sumIncomeMoney(@RequestBody IncomeMoneySumQuery query);

    // ==================== ReconciliationController ====================

    @GetMapping("/pay/admin/reconciliation/reconciliationJobLogSumQuery")
    public Result<List<ReconciliationJobLogSumVO>> reconciliationJobLogSumQuery(@SpringQueryMap ReconciliationJobLogSumQuery query);

    @GetMapping("/pay/admin/reconciliation/reconciliationDiffPageQuery")
    public Result<PageResult<ReconciliationDiffPageVO>> reconciliationDiffPageQuery(@SpringQueryMap ReconciliationDiffPageQuery query);

    @GetMapping("/pay/admin/reconciliation/channelBillDownloadProgressListQuery")
    public Result<List<ChannelBillDownloadProgressListVO>> channelBillDownloadProgressListQuery(@SpringQueryMap ChannelBillDownloadProgressListQuery query);

    @GetMapping("/pay/admin/reconciliation/signCustomerFundBillDetailPageQuery")
    public Result<PageResult<SignCustomerFundBillDetailPageVO>> signCustomerFundBillDetailPageQuery(@SpringQueryMap SignCustomerFundBillDetailPageQuery query);

    @GetMapping("/pay/admin/reconciliation/billTradeSynchronizerTask")
    public Result<Void> billTradeSynchronizerTask(@RequestParam("bathId") String bathId);

    @GetMapping("/pay/admin/reconciliation/isTradeAllBillParsedTask")
    public Result<Void> isTradeAllBillParsedTask(@RequestParam("bathId") String bathId);

    // ==================== WalletWithdrawController ====================

    @GetMapping("/pay/admin/walletWithdraw/walletWithdrawPageQuery")
    public Result<PageResult<WalletWithdrawPageVO>> walletWithdrawPageQuery(@SpringQueryMap WalletWithdrawPageQuery query);





}

