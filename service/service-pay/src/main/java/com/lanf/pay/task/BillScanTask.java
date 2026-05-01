package com.lanf.pay.task;

import com.lanf.common.utils.BeanUtil;
import com.lanf.pay.model.enums.ReconciliationJobTypeEnum;
import com.lanf.pay.service.reconciliation.strategy.ReconciliationStrategy;
import com.lanf.pay.service.reconciliation.strategy.ReconciliationStrategyFactory;

public class BillScanTask implements Runnable{

    private final String bathId;
    private final ReconciliationJobTypeEnum jobType;


    private final ReconciliationStrategyFactory reconciliationStrategyFactory;

    public BillScanTask(String bathId, ReconciliationJobTypeEnum jobType) {
        this.bathId = bathId;
        this.jobType = jobType;
        this.reconciliationStrategyFactory = BeanUtil.getBean(ReconciliationStrategyFactory.class);
    }

    @Override
    public void run() {

        ReconciliationStrategy strategy = reconciliationStrategyFactory.getStrategy(jobType);
        strategy.startScan(bathId);
    }
}
