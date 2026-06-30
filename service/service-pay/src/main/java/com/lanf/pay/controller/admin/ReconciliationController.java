package com.lanf.pay.controller.admin;

import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import com.lanf.pay.mapper.ChannelBillDownloadProgressMapper;
import com.lanf.pay.mapper.SignCustomerFundBillDetailMapper;
import com.lanf.pay.model.query.ReconciliationDiffPageQuery;
import com.lanf.pay.model.query.ReconciliationJobLogSumQuery;
import com.lanf.pay.model.vo.ReconciliationDiffPageVO;
import com.lanf.pay.model.vo.ReconciliationJobLogSumVO;
import com.lanf.pay.service.reconciliation.IReconciliationDiffService;
import com.lanf.pay.service.reconciliation.IReconciliationJobLogService;
import com.lanf.pay.service.reconciliation.excel.ExcelParseProgressManager;
import com.lanf.pay.task.BatchIdContext;
import com.lanf.pay.task.SignCustomerBillReconciliationTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/reconciliation")
public class ReconciliationController {

    @Autowired
    private IReconciliationJobLogService reconciliationJobLogService;
    @Autowired
    private IReconciliationDiffService reconciliationDiffService;
    @Autowired
    private SignCustomerBillReconciliationTask signCustomerBillReconciliationTask;
    @Autowired
    private ExcelParseProgressManager excelParseProgressManager;
    @Autowired
    private ChannelBillDownloadProgressMapper channelBillDownloadProgressMapper;
    @Autowired
    private SignCustomerFundBillDetailMapper signCustomerFundBillDetailMapper;



    @GetMapping("/reconciliationJobLogSumQuery")
    public Result<List<ReconciliationJobLogSumVO>> reconciliationJobLogSumQuery(@Validated ReconciliationJobLogSumQuery query) {

        log.info("查询对账任务:{}", query);

        return Result.ok(reconciliationJobLogService.reconciliationJobLogSumQuery(query));
    }

    @GetMapping("/reconciliationDiffPageQuery")
    public Result<PageResult<ReconciliationDiffPageVO>> reconciliationDiffPageQuery(@Validated ReconciliationDiffPageQuery query) {

        log.info("分页查询对账差异:{}", query);

        return Result.ok(reconciliationDiffService.reconciliationDiffPageQuery(query));
    }

    /**
     * 手动开启账单下载任务
     *
     *
     */
    @GetMapping("/billTradeSynchronizerTask")
    public Result<Void> billTradeSynchronizerTask(@Validated String bathId) {

        log.info("手动开启账单下载任务:{}", bathId);
        excelParseProgressManager.deleteAllProgressKeys();
        channelBillDownloadProgressMapper.deleteAll();
        BatchIdContext.setBatchId(bathId);
        signCustomerBillReconciliationTask.billTradeSynchronizerTask();
        return Result.ok();
    }

}
