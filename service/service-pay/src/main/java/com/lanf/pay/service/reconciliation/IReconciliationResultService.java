package com.lanf.pay.service.reconciliation;

/**
 * <p>
 * 对账结果表 服务类
 * </p>
 *
 * @author jarven
 * @since 2026-04-30
 */
public interface IReconciliationResultService  {

    void addReconciliationResultAndJobLog(String batchId);

}
