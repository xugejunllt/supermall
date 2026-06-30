package com.lanf.pay.service.reconciliation.impl;

import com.lanf.constant.utils.IdUtils;
import com.lanf.pay.mapper.ReconciliationJobLogMapper;
import com.lanf.pay.model.entity.ReconciliationJobLogDO;
import com.lanf.pay.model.enums.ReconciliationJobStatusEnum;
import com.lanf.pay.model.enums.ReconciliationJobTypeEnum;
import com.lanf.pay.service.reconciliation.IReconciliationJobLogService;
import com.lanf.pay.service.reconciliation.IReconciliationResultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
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
public class ReconciliationResultServiceImpl implements IReconciliationResultService {

    @Autowired
    private IReconciliationJobLogService reconciliationJobLogService;
    @Autowired
    private ReconciliationJobLogMapper reconciliationJobLogMapper;
    @Transactional
    @Override
    public void addReconciliationResultAndJobLog(String bathId) {

        List<ReconciliationJobLogDO> jobLogs = new ArrayList<>();

        for (ReconciliationJobTypeEnum jobType : ReconciliationJobTypeEnum.values()) {
            ReconciliationJobLogDO jobLog = new ReconciliationJobLogDO();
            jobLog.setBatchId(bathId);
            jobLog.setJobType(jobType);
            jobLog.setStatus(ReconciliationJobStatusEnum.EXECUTING);
            jobLogs.add(jobLog);
        }
        jobLogs.forEach(a -> {
            a.setId(IdUtils.generateId());
            Date date = new Date();
            a.setCreateTime(new Date());
            a.setUpdateTime(date);
        });
        reconciliationJobLogMapper.batchInsertIgnore(jobLogs);

        log.info("批次号 {} 的对账结果和任务日志已初始化完成", bathId);
    }


}
