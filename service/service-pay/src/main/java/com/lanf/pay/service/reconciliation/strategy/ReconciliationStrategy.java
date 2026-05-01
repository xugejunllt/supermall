package com.lanf.pay.service.reconciliation.strategy;

import com.lanf.pay.model.enums.ReconciliationJobTypeEnum;

/**
 * 对账扫描策略接口
 */
public interface ReconciliationStrategy {

    ReconciliationJobTypeEnum getJobType();

    void startScan(String bathId);
}
