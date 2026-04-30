package com.lanf.pay.service.reconciliation;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.pay.model.entity.ReconciliationResultDO;

/**
 * <p>
 * 对账结果表 服务类
 * </p>
 *
 * @author jarven
 * @since 2026-04-30
 */
public interface IReconciliationResultService extends IService<ReconciliationResultDO> {

    void addReconciliationResultAndJobLog(String batchId);

}
