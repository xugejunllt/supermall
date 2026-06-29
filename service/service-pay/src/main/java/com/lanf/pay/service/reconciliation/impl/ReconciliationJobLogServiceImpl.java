package com.lanf.pay.service.reconciliation.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.JsonUtils;
import com.lanf.pay.mapper.ReconciliationJobLogMapper;
import com.lanf.pay.model.bo.SendMessageAndUpdateResult;
import com.lanf.pay.model.entity.ReconciliationJobLogDO;
import com.lanf.pay.model.enums.ReconciliationJobStatusEnum;
import com.lanf.pay.model.enums.ReconciliationJobTypeEnum;
import com.lanf.pay.mq.constant.PayMqTopicName;
import com.lanf.pay.mq.message.ReconciliationStartMessage;
import com.lanf.pay.service.reconciliation.IReconciliationJobLogService;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 对账任务执行记录表 服务实现类
 * </p>
 *
 * @author jarven
 * @since 2026-04-30
 */
@Slf4j
@Service
public class ReconciliationJobLogServiceImpl extends ServiceImpl<ReconciliationJobLogMapper, ReconciliationJobLogDO> implements IReconciliationJobLogService {


    @Autowired
    private RocketMqClient rocketMqClient;
    @Lazy
    @Autowired
    private IReconciliationJobLogService reconciliationJobLogService;

    /**
     *
     * mq发送 与更新 在同个事务里 异常进行回滚
     */
    @Transactional
    @Override
    public SendMessageAndUpdateResult sendMessageAndUpdate(ReconciliationStartMessage message, ReconciliationJobTypeEnum jobType, String bathId, long currentPage, long pages) throws Exception {
        rocketMqClient.sendMessage(PayMqTopicName.RECONCILIATION_START_TOPIC, JsonUtils.
                toJsonString(message));

        if (currentPage >= pages) {
            /**
             * 更新jbs状态为已完成
             */
            reconciliationJobLogService.lambdaUpdate()
                    .eq(ReconciliationJobLogDO::getBatchId, bathId)
                    .eq(ReconciliationJobLogDO::getJobType, jobType)
                    .set(ReconciliationJobLogDO::getStatus, ReconciliationJobStatusEnum.SCAN_COMPLETED)
                    .update();

            SendMessageAndUpdateResult result = new SendMessageAndUpdateResult();
            result.setToBreak(true);

            log.info("批次号 {} 批次 {} 扫描任务已完成", bathId, jobType);
            return result;
        }
        SendMessageAndUpdateResult result = new SendMessageAndUpdateResult();
        result.setToBreak(false);


        return result;
    }
}
