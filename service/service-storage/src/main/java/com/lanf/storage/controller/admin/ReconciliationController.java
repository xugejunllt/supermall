package com.lanf.storage.controller.admin;

import com.lanf.api.storage.model.query.ReconciliationDiffPageQuery;
import com.lanf.api.storage.model.query.ReconciliationOrderDetailPageQuery;
import com.lanf.api.storage.model.vo.ReconciliationDiffPageVO;
import com.lanf.api.storage.model.vo.ReconciliationOrderDetailPageVO;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import com.lanf.storage.mapper.ReconciliationDiffMapper;
import com.lanf.storage.service.reconciliation.IReconciliationDiffService;
import com.lanf.storage.service.reconciliation.IReconciliationOrderDetailService;
import com.lanf.storage.task.StockReconciliationTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/admin/reconciliation")
public class ReconciliationController {

    @Autowired
    private IReconciliationDiffService reconciliationDiffService;
    @Autowired
    private StockReconciliationTask stockReconciliationTask;

    @Autowired
    private IReconciliationOrderDetailService reconciliationOrderDetailService;
    @Autowired
    private ReconciliationDiffMapper reconciliationDiffMapper;
    // ==================== ReconciliationDiff 对账差异管理 ====================

    @GetMapping("/reconciliationDiffPageQuery")
    public Result<PageResult<ReconciliationDiffPageVO>> reconciliationDiffPageQuery(ReconciliationDiffPageQuery query) {
        log.info("分页查询对账差异列表:query{}", query);
        return Result.ok(reconciliationDiffService.reconciliationDiffPageQuery(query));
    }

    // ==================== ReconciliationOrderDetail 库存对账订单详细管理 ====================

    @GetMapping("/reconciliationOrderDetailPageQuery")
    public Result<PageResult<ReconciliationOrderDetailPageVO>> reconciliationOrderDetailPageQuery(ReconciliationOrderDetailPageQuery query) {
        log.info("分页查询库存对账订单详细列表:query{}", query);
        return Result.ok(reconciliationOrderDetailService.reconciliationOrderDetailPageQuery(query));
    }




    @GetMapping("/shortStockReconciliationScanTask")
    public Result<Void> shortStockReconciliationScanTask(@RequestParam("batchId") Long batchId) {
        log.info("手动开启短库存对账任务:batchId{}", batchId);
        reconciliationDiffMapper.deleteAll();
        stockReconciliationTask.shortStockReconciliationScanTask();
        return Result.ok();
    }


    @GetMapping("/longStockReconciliationScanTask")
    public Result<Void> longStockReconciliationScanTask(@RequestParam("batchId") Long batchId) {

        log.info("手动开启长库存对账任务:batchId{}", batchId);
        reconciliationDiffMapper.deleteAll();
        stockReconciliationTask.longStockReconciliationScanTask();
        return Result.ok();
    }


}
