package com.lanf.rocketmq.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lanf.rocketmq.model.entity.MqConsumeMessageDO;
import com.lanf.rocketmq.sevice.IMqConsumeMessageService;
import com.lanf.rocketmq.sevice.MqRetryInstanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * MQ消费消息扫描定时任务
 * <p>每10分钟扫描一次消费成功/失败且预计完成时间已超前的消息</p>
 * <p>编号由 {@link MqMessageRetryAllocateTask} 统一分配，本任务只负责读取</p>
 */
@Slf4j
@Component
public class MqConsumeMessageRetryTask {

    @Autowired
    private IMqConsumeMessageService mqConsumeMessageService;

    @Autowired
    private MqRetryInstanceService mqRetryInstanceService;

    /**
     * 每10分钟执行一次
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    public void retryPendingMessages() {
        log.info("开始扫描MqConsumeMessageDO，检查需要处理的消息");

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
        Date nowMinus5Min = new Date(System.currentTimeMillis() - 5 * 60 * 1000L);
        int totalCount = 0;

        while (true) {
            Page<MqConsumeMessageDO> page = new Page<>(current, size);
            QueryWrapper<MqConsumeMessageDO> wrapper = new QueryWrapper<>();
            wrapper.eq("status", 0);
            //当前时间 大于nextEstimatedCompletionAt+5分钟 空出5分钟 让延迟任务执行完毕
            wrapper.lt("next_estimated_completion_at", nowMinus5Min);

            Page<MqConsumeMessageDO> result = mqConsumeMessageService.page(page, wrapper);
            List<MqConsumeMessageDO> records = result.getRecords();

            if (records == null || records.isEmpty()) {
                break;
            }

            for (MqConsumeMessageDO messageDO : records) {
                try {
                    // 4. 取模匹配，只处理属于自己编号的消息
                    if (messageDO.getId() % instanceCount != serviceNumber) {
                        continue;
                    }

                    log.info("发现待处理MqConsumeMessageDO，id:{}, messageId:{}, status:{}",
                            messageDO.getId(), messageDO.getMessageId(), messageDO.getStatus());
                    // TODO: 根据业务需求处理消息
                    totalCount++;
                } catch (Exception e) {
                    log.error("处理MqConsumeMessageDO失败，id:{}", messageDO.getId(), e);
                }
            }

            if (!result.hasNext()) {
                break;
            }
            current++;
        }

        log.info("MqConsumeMessageDO扫描完成，共发现{}条需要处理的消息", totalCount);
    }
}
