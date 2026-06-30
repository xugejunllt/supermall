package com.lanf.pay.service.reconciliation.strategy;

import com.lanf.pay.model.bo.ReconciliationStart;
import com.lanf.api.pay.model.enums.ReconciliationJobTypeEnum;

/**
 *
 */
public interface ReconciliationStrategy {

    ReconciliationJobTypeEnum getJobType();
    /**
     * 开始扫描
     *
     */
    void startScan(String bathId);

    /**
     * 开始对账
     *
     */
    void startReconciliation(ReconciliationStart  start);
}
