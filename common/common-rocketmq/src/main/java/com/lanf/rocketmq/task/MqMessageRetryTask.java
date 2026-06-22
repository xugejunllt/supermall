package com.lanf.rocketmq.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lanf.rocketmq.model.entity.MqSendMessageDO;
import com.lanf.rocketmq.sevice.IMqSendMessageService;
import com.lanf.rocketmq.sevice.MqRetryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * MQ消息重试定时任务
 * <p>每5分钟扫描一次待发送且超时的消息，触发重试</p>
 */
@Slf4j
@Component
public class MqMessageRetryTask {

    @Autowired
    private IMqSendMessageService mqSendMessageService;

    @Autowired
    private MqRetryService mqRetryService;

    /**
     * 每5分钟执行一次
     */
    @Scheduled(cron = "0 0/5 * * * ?")
    public void retryPendingMessages() {
        log.info("开始扫描待发送MQ消息，准备重试");

        int current = 1;
        int size = 100;
        Date tenMinutesAgo = new Date(System.currentTimeMillis() - 10 * 60 * 1000L);
        int totalRetryCount = 0;

        while (true) {
            Page<MqSendMessageDO> page = new Page<>(current, size);
            QueryWrapper<MqSendMessageDO> wrapper = new QueryWrapper<>();
            wrapper.eq("status", 0);
            wrapper.lt("retry_count", 3);
            wrapper.lt("create_time", tenMinutesAgo);

            Page<MqSendMessageDO> result = mqSendMessageService.page(page, wrapper);
            List<MqSendMessageDO> records = result.getRecords();

            if (records == null || records.isEmpty()) {
                break;
            }

            for (MqSendMessageDO messageDO : records) {
                try {
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

        log.info("待发送MQ消息扫描完成，共触发{}条消息重试", totalRetryCount);
    }
}
