package com.lanf.pay.service.reconciliation.strategy;

import com.lanf.pay.model.bo.ReconciliationStart;

/**
 *
 */
public interface ReconciliationStrategy {


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
