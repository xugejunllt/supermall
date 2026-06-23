package com.lanf.rocketmq.task;

import com.lanf.cache.service.DistributedLocker;
import com.lanf.rocketmq.sevice.MqRetryInstanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * MQ消息重试编号分配定时任务
 * <p>每1分钟执行一次，负责给当前服务的所有实例分配编号</p>
 */
@Slf4j
@Component
public class MqMessageRetryAllocateTask {

    @Autowired
    private DistributedLocker distributedLocker;

    @Autowired
    private MqRetryInstanceService mqRetryInstanceService;

    /**
     * 每1分钟执行一次，分配编号
     */
    @Scheduled(cron = "0/10 * * * * ?")
    public void allocateServiceNumbers() {
        String lockKey = mqRetryInstanceService.getLockKey();
        boolean locked = false;
        try {
            // 1. 获取分布式锁，保证只有一个节点执行编号分配
            locked = distributedLocker.getLock(lockKey);
            if (!locked) {
                log.debug("获取分布式锁失败，跳过本次编号分配，serviceName:{}", mqRetryInstanceService.getServiceName());
                return;
            }

            log.info("开始执行MQ消息重试编号分配，serviceName:{}", mqRetryInstanceService.getServiceName());

            // 2. 从Nacos获取当前服务的所有实例
            List<MqRetryInstanceService.InstanceInfo> instances = mqRetryInstanceService.getInstancesFromNacos();
            if (instances.isEmpty()) {
                log.warn("未获取到在线服务实例，跳过编号分配，serviceName:{}", mqRetryInstanceService.getServiceName());
                return;
            }

            // 3. 按IP+端口排序，保证编号分配的稳定性
            mqRetryInstanceService.sortInstances(instances);

            // 4. 分配编号并写入Redis
            mqRetryInstanceService.allocateNumbers(instances);

            log.info("MQ消息重试编号分配完成，serviceName:{}, 实例数:{}", mqRetryInstanceService.getServiceName(), instances.size());
        } catch (Exception e) {
            log.error("MQ消息重试编号分配异常，serviceName:{}", mqRetryInstanceService.getServiceName(), e);
        } finally {
            if (locked) {
                distributedLocker.unlock(lockKey);
            }
        }
    }
}
