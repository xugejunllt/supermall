package com.lanf.system.controller.pay;

import com.lanf.api.pay.api.PayApiService;
import com.lanf.api.pay.model.dto.AddPayAccountDTO;
import com.lanf.api.pay.model.query.*;
import com.lanf.api.pay.model.vo.*;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/pay")
public class PayAdminController {

    @Autowired
    private PayApiService payApiService;

    @PostMapping("/addPayAccount")
    public Result<Void> addPayAccount(@Validated @RequestBody AddPayAccountDTO dto) {

        log.info("添加支付账户");
        return payApiService.addPayAccount(dto);
    }

    @GetMapping("/payAccountPageQuery")
    public Result<PageResult<PayAccountPageVO>> payAccountPageQuery(@Validated PayAccountPageQuery query) {
        log.info("分页查询支付账户");

        return payApiService.payAccountPageQuery(query);
    }

    // ==================== PaymentSummaryController ====================

    @GetMapping("/paymentSummary/paymentSummaryQuery")
    public Result<PaymentSummarySumVO> paymentSummaryQuery(@Validated PaymentSummaryQuery query) {
        log.info("统计平台收支金额");
        return payApiService.paymentSummaryQuery(query);
    }

    @GetMapping("/paymentSummary/clearingDetailPageQuery")
    public Result<PageResult<ClearingDetailPageVO>> clearingDetailPageQuery(@Validated ClearingDetailPageQuery query) {
        log.info("分页查询清算明细");
        return payApiService.clearingDetailPageQuery(query);
    }

    @PostMapping("/paymentSummary/sumIncomeMoney")
    public Result<BigDecimal> sumIncomeMoney(@RequestBody IncomeMoneySumQuery query) {
        log.info("根据创建时间区间统计收入金额");
        return payApiService.sumIncomeMoney(query);
    }

    // ==================== ReconciliationController ====================

    @GetMapping("/reconciliation/reconciliationJobLogSumQuery")
    public Result<List<ReconciliationJobLogSumVO>> reconciliationJobLogSumQuery(@Validated ReconciliationJobLogSumQuery query) {
        log.info("查询对账任务");
        return payApiService.reconciliationJobLogSumQuery(query);
    }

    @GetMapping("/reconciliation/reconciliationDiffPageQuery")
    public Result<PageResult<ReconciliationDiffPageVO>> reconciliationDiffPageQuery(@Validated ReconciliationDiffPageQuery query) {
        log.info("分页查询对账差异");
        return payApiService.reconciliationDiffPageQuery(query);
    }

    @GetMapping("/reconciliation/channelBillDownloadProgressListQuery")
    public Result<List<ChannelBillDownloadProgressListVO>> channelBillDownloadProgressListQuery(@Validated ChannelBillDownloadProgressListQuery query) {
        log.info("分页查询账单下载进度");
        return payApiService.channelBillDownloadProgressListQuery(query);
    }

    @GetMapping("/reconciliation/signCustomerFundBillDetailPageQuery")
    public Result<PageResult<SignCustomerFundBillDetailPageVO>> signCustomerFundBillDetailPageQuery(@Validated SignCustomerFundBillDetailPageQuery query) {
        log.info("分页查询资金账单明细");
        return payApiService.signCustomerFundBillDetailPageQuery(query);
    }

    @GetMapping("/reconciliation/billTradeSynchronizerTask")
    public Result<Void> billTradeSynchronizerTask(@RequestParam("bathId") String bathId) {
        log.info("手动开启账单下载任务");
        return payApiService.billTradeSynchronizerTask(bathId);
    }

    @GetMapping("/reconciliation/isTradeAllBillParsedTask")
    public Result<Void> isTradeAllBillParsedTask(@RequestParam("bathId") String bathId) {
        log.info("手动开启对账任务");
        return payApiService.isTradeAllBillParsedTask(bathId);
    }

    // ==================== WalletWithdrawController ====================

    @GetMapping("/walletWithdraw/walletWithdrawPageQuery")
    public Result<PageResult<WalletWithdrawPageVO>> walletWithdrawPageQuery(@Validated WalletWithdrawPageQuery query) {
        log.info("分页查询钱包提现记录");
        return payApiService.walletWithdrawPageQuery(query);
    }


}
