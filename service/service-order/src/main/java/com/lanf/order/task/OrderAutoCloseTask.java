package com.lanf.order.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lanf.aftersales.mq.message.CloseOrderMessage;
import com.lanf.api.order.mq.constant.OrderClientTopicName;
import com.lanf.common.utils.JsonUtils;
import com.lanf.order.model.entity.OrderAutoCloseDO;
import com.lanf.order.model.enums.OrderAutoCloseStatusEnum;
import com.lanf.order.service.order.IOrderAutoCloseService;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * 订单自动关闭定时任务
 *
 * @author jarven
 * @since 2026-07-04
 */
@Slf4j
@Component
public class OrderAutoCloseTask {

    @Autowired
    private IOrderAutoCloseService orderAutoCloseService;

    @Autowired
    private RocketMqClient rocketMqClient;
    /**
     * 每天凌晨0点扫描已到期的订单自动关闭记录
     * 过滤条件：当前时间 >= autoCloseTime 且 status = COMPLETED
     */
    @Scheduled(cron = "0 0 0 * * ?", zone = "Asia/Shanghai")
    public void orderAutoCloseScanTask() {
        log.info("订单自动关闭扫描任务开始执行");
        Date now = new Date();

        long pageNum = 1;
        long pageSize = 10;
        Page<OrderAutoCloseDO> page;

        do {
            LambdaQueryWrapper<OrderAutoCloseDO> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.le(OrderAutoCloseDO::getAutoCloseTime, now)
                    .eq(OrderAutoCloseDO::getStatus, OrderAutoCloseStatusEnum.COMPLETED)
                    .orderByAsc(OrderAutoCloseDO::getId);

            page = orderAutoCloseService.page(new Page<>(pageNum, pageSize), queryWrapper);

            List<OrderAutoCloseDO> recordList = page.getRecords();
            if (recordList != null && !recordList.isEmpty()) {
                log.info("第 {} 页扫描到 {} 条待处理记录", pageNum, recordList.size());
                recordList.forEach(record -> {
                    log.info("开始处理订单自动关闭：{}", record);
                    CloseOrderMessage closeOrderMessage = new CloseOrderMessage();
                    closeOrderMessage.setOrderId(record.getOrderId());
                    closeOrderMessage.setUserId(record.getUserId());
                    rocketMqClient.sendMessage(OrderClientTopicName.CLOSE_ORDER_TOPIC,
                            JsonUtils.toJsonString(closeOrderMessage));
                });
            }

            pageNum++;
        } while (page.getCurrent() < page.getPages());

        log.info("订单自动关闭扫描任务执行完毕");
    }
}
