package com.lanf.rocketmq.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lanf.rocketmq.model.entity.MqSendMessageDO;
import com.lanf.rocketmq.sevice.IMqSendMessageService;
import com.lanf.rocketmq.sevice.MqRetryInstanceService;
import com.lanf.rocketmq.sevice.MqRetryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * MQ消息重试定时任务
 * <p>每10秒扫描一次正在发送中且预计完成时间已超前的消息，触发重试</p>
 * <p>编号由 {@link MqMessageRetryAllocateTask} 统一分配，本任务只负责读取</p>
 */
@Slf4j
@Component
public class MqMessageRetryTask {

    @Autowired
    private IMqSendMessageService mqSendMessageService;

    @Autowired
    private MqRetryService mqRetryService;

    @Autowired
    private MqRetryInstanceService mqRetryInstanceService;

    /**
     * 每10分钟执行一次
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    public void retryPendingMessages() {
        log.info("开始扫描正在发送中的MQ消息，准备重试");

        // 1. 获取当前实例的编号
        Integer serviceNumber = mqRetryInstanceService.getServiceNumber();
        if (serviceNumber == null) {
            log.warn("当前实例编号未分配，跳过本次扫描，等待MqMessageRetryAllocateTask分配编号");
            return;
        }

        // 2. 获取实例总数（用于取模）
        Integer instanceCount = mqRetryInstanceService.getInstanceCount();
        if (instanceCount == null || instanceCount <= 0) {
            log.warn("实例总数未获取到，跳过本次扫描");
            return;
        }

        log.info("当前服务编号: {}/{}, serviceName: {}", serviceNumber, instanceCount, mqRetryInstanceService.getServiceName());

        // 3. 扫描消息并处理
        int current = 1;
        int size = 100;
        // 超时阈值：当前时间往前推5分钟，用于判断消息是否已超时
        // 如果消息的预期完成时间早于这个阈值，说明消息发送已超时，需要补偿重试
        Date overdueThreshold = new Date(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(5));
        int totalRetryCount = 0;

        while (true) {
            Page<MqSendMessageDO> page = new Page<>(current, size);
            QueryWrapper<MqSendMessageDO> wrapper = new QueryWrapper<>();
            wrapper.eq("status", 0);
            wrapper.lt("next_estimated_completion_at", overdueThreshold);
            wrapper.lt("retry_count", 3);

            Page<MqSendMessageDO> result = mqSendMessageService.page(page, wrapper);
            List<MqSendMessageDO> records = result.getRecords();

            if (records == null || records.isEmpty()) {
                break;
            }

            for (MqSendMessageDO messageDO : records) {
                try {
                    // 4. 取模匹配，只处理属于自己编号的消息
                    if (messageDO.getId() % instanceCount != serviceNumber) {
                        continue;
                    }

                    int nextRetryCount = messageDO.getRetryCount() + 1;
                    log.info("发现待重试MQ消息，messageId:{}, currentRetryCount:{}, nextRetryCount:{}",
                            messageDO.getId(), messageDO.getRetryCount(), nextRetryCount);
                    mqRetryService.addToRetryQueue(messageDO, nextRetryCount);
                    totalRetryCount++;
                } catch (Exception e) {
                    log.error("重试消息失败，messageId:{}", messageDO.getId(), e);
                }
            }

            if (!result.hasNext()) {
                break;
            }
            current++;
        }

        log.info("正在发送中的MQ消息扫描完成，共触发{}条消息重试", totalRetryCount);
    }
}
