package com.lanf.pay.controller.admin;


import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import com.lanf.pay.model.query.ClearingDetailPageQuery;
import com.lanf.pay.model.query.IncomeMoneySumQuery;
import com.lanf.pay.model.query.PaymentSummaryQuery;
import com.lanf.pay.model.vo.ClearingDetailPageVO;
import com.lanf.pay.model.vo.PaymentSummarySumVO;
import com.lanf.pay.service.clearing.ClearingDetailService;
import com.lanf.pay.service.pay.IPayOrderFlowService;
import com.lanf.pay.service.pay.IRefundOrderFlowService;
import com.lanf.pay.service.pay.ITransferOrderFlowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Slf4j
@RestController
@RequestMapping("/admin/paymentSummary")
public class PaymentSummaryController {

    @Autowired
    private IPayOrderFlowService payOrderFlowService;
    @Autowired
    private IRefundOrderFlowService refundOrderFlowService;
    @Autowired
    private ITransferOrderFlowService transferOrderFlowService;
    @Autowired
    private ClearingDetailService clearingDetailService;


    /**
     * 统计平台收支金额
     *
     *
     */

    @GetMapping("/paymentSummaryQuery")
    public Result<PaymentSummarySumVO> paymentSummaryQuery(PaymentSummaryQuery query) {

        log.info("统计统计平台收支金额:{}", query);
        String batchId = query.getBatchId();

        //1.查询收入金额（支付流水实收金额）
        BigDecimal incomeAmount = payOrderFlowService.sumReceiptMoney(batchId);

        //2.查询退款金额
        BigDecimal refundAmount = refundOrderFlowService.sumReturnMoney(batchId);

        //3.查询转账金额
        BigDecimal transferAmount = transferOrderFlowService.sumTotalAmount(batchId);

        //4.计算支付金额（退款+转账）
        BigDecimal paymentAmount = refundAmount.add(transferAmount);

        //5.计算净收入
        BigDecimal netIncome = incomeAmount.subtract(paymentAmount);

        PaymentSummarySumVO vo = new PaymentSummarySumVO();
        vo.setIncomeAmount(incomeAmount);
        vo.setPaymentAmount(paymentAmount);
        vo.setNetIncome(netIncome);

        return Result.ok(vo);
    }

    /**
     * 商户查询 分页查询清算明细
     */
    @GetMapping("/clearingDetailPageQuery")
    public Result<PageResult<ClearingDetailPageVO>> clearingDetailPageQuery(ClearingDetailPageQuery query) {
        log.info("分页查询清算明细:{}", query);
        return Result.ok(clearingDetailService.clearingDetailPageQuery(query));
    }

    /**
     * 根据创建时间区间统计收入金额
     */
    @PostMapping("/sumIncomeMoney")
    public Result<BigDecimal> sumIncomeMoney(@RequestBody IncomeMoneySumQuery query) {
        log.info("根据创建时间区间统计收入金额:{}", query);
        BigDecimal sum = clearingDetailService.sumIncomeMoney(query.getStartTime(), query.getEndTime());
        return Result.ok(sum);
    }
















}
