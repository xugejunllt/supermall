package com.lanf.pay.service.reconciliation;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.pay.model.bo.SendMessageAndUpdateResult;
import com.lanf.pay.model.entity.ReconciliationJobLogDO;
import com.lanf.pay.model.enums.ReconciliationJobTypeEnum;
import com.lanf.pay.model.query.ReconciliationJobLogSumQuery;
import com.lanf.pay.model.vo.ReconciliationJobLogSumVO;
import com.lanf.pay.mq.message.ReconciliationStartMessage;

import java.util.List;

/**
 * <p>
 * 对账任务执行记录表 服务类
 * </p>
 *
 * @author jarven
 * @since 2026-04-30
 */
public interface IReconciliationJobLogService extends IService<ReconciliationJobLogDO> {


    SendMessageAndUpdateResult sendMessageAndUpdate(ReconciliationStartMessage  message, ReconciliationJobTypeEnum jobType,
                                                    String bathId, long currentPage, long  pages) throws Exception;

    /**
     * 统计对账任务进度
     *
     */
    List<ReconciliationJobLogSumVO> reconciliationJobLogSumQuery(ReconciliationJobLogSumQuery query);

}
