package com.lanf.pay.service.reconciliation.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.pay.mapper.ReconciliationResultMapper;
import com.lanf.pay.model.entity.ReconciliationJobLogDO;
import com.lanf.pay.model.entity.ReconciliationResultDO;
import com.lanf.pay.model.enums.ReconciliationJobStatusEnum;
import com.lanf.pay.model.enums.ReconciliationJobTypeEnum;
import com.lanf.pay.model.enums.ReconciliationStatusEnum;
import com.lanf.pay.service.reconciliation.IReconciliationJobLogService;
import com.lanf.pay.service.reconciliation.IReconciliationResultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 对账结果表 服务实现类
 * </p>
 *
 * @author jarven
 * @since 2026-04-30
 */
@Slf4j
@Service
public class ReconciliationResultServiceImpl extends ServiceImpl<ReconciliationResultMapper, ReconciliationResultDO> implements IReconciliationResultService {

    @Autowired
    private IReconciliationJobLogService reconciliationJobLogService;

    @Transactional
    @Override
    public void addReconciliationResultAndJobLog(String bathId) {

        ReconciliationResultDO reconciliationResult = buildReconciliationResult(bathId);
        List<ReconciliationJobLogDO> jobLogs = new ArrayList<>();

        for (ReconciliationJobTypeEnum jobType : ReconciliationJobTypeEnum.values()) {
            ReconciliationJobLogDO jobLog = new ReconciliationJobLogDO();
            jobLog.setBatchId(bathId);
            jobLog.setJobType(jobType);
            jobLog.setStatus(ReconciliationJobStatusEnum.EXECUTING);
            jobLogs.add(jobLog);
        }
        try {
            this.save(reconciliationResult);
            reconciliationJobLogService.saveBatch(jobLogs);
        } catch (DuplicateKeyException e) {
            log.warn("重复初始化");
            return;
        }
        log.info("批次号 {} 的对账结果和任务日志已初始化完成", bathId);
    }

    private ReconciliationResultDO buildReconciliationResult(String bathId) {
        ReconciliationResultDO reconciliationResult = new ReconciliationResultDO();
        reconciliationResult.setBatchId(bathId);
        reconciliationResult.setTotalMyCount(0);
        reconciliationResult.setTotalMyAmount(BigDecimal.ZERO);
        reconciliationResult.setTotalChannelCount(0);
        reconciliationResult.setTotalChannelAmount(BigDecimal.ZERO);
        reconciliationResult.setDiffCountLong(0);
        reconciliationResult.setDiffCountShort(0);
        reconciliationResult.setDiffCountAmount(0);
        reconciliationResult.setStatus(ReconciliationStatusEnum.PROCESSING);
        return reconciliationResult;
    }
}
