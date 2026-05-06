package com.lanf.order.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lanf.common.utils.DateUtils;
import com.lanf.common.utils.IStringUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.order.model.entity.OrderStatusTraceDO;
import com.lanf.order.model.enums.OrderStatusEnum;
import com.lanf.order.service.IOrderStatusTraceService;
import com.lanf.rocketmq.util.RocketMqClient;
import com.lanf.storage.model.enums.ReconciliationOrderStatusEnum;
import com.lanf.storage.mq.constant.StorageClientTopicName;
import com.lanf.storage.mq.message.BuildReconciliationOrderDetailMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * 库存对账 扫描订单轨迹
 *
 */
@Slf4j
@Component
public class StockReconciliationScanOrderStatusTraceTask {

    @Autowired
    private IOrderStatusTraceService orderStatusTraceService;
    @Autowired
    private RocketMqClient rocketMqClient;

    /**
     * 参与对账的订单状态
     */
    private static List<OrderStatusEnum> STATUS_LIST = Arrays.asList(
            OrderStatusEnum.WAIT_PAY,
            OrderStatusEnum.OUTBOUNDED,
            OrderStatusEnum.CANCELLED
    );



    @Scheduled(cron = "0 0 9 * * ?")
    public void scanOrderStatusTraceTask() {
        String createDate = DateUtils.getRelativeDateString(new Date(), -1, DateUtils.DATE);
        
        long pageNum = 1;
        long pageSize = 100;
        LambdaQueryWrapper<OrderStatusTraceDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderStatusTraceDO::getCreateDate, createDate)
                .in(OrderStatusTraceDO::getToStatus, STATUS_LIST)
                .orderByAsc(OrderStatusTraceDO::getId);

        Page<OrderStatusTraceDO> page = orderStatusTraceService.page(new Page<>(pageNum, pageSize), queryWrapper);
        
        do {

            try {
                page = orderStatusTraceService.page(new Page<>(pageNum, pageSize), queryWrapper);

                List<OrderStatusTraceDO> traceList = page.getRecords();
                if ( !IStringUtils.isEmpty(traceList)) {
                    List<BuildReconciliationOrderDetailMessage> messageList = convertToReconciliationMessage(traceList);
                    rocketMqClient.sendMessage(StorageClientTopicName.BUILD_RECONCILIATION_ORDER_DETAIL_EVENT_TOPIC,
                            JsonUtils.toJsonString(messageList));


                }
            } catch (Exception e) {
                  log.warn("库存对账 扫描订单轨迹异常", e);
                  continue;
            }

            pageNum++;
        } while (page.getCurrent() < page.getPages());
    }

    /**
     * 将订单轨迹列表转换为对账订单明细消息列表
     */
    private List<BuildReconciliationOrderDetailMessage> convertToReconciliationMessage(List<OrderStatusTraceDO> traceList) {
        return traceList.stream().map(trace -> {
            BuildReconciliationOrderDetailMessage message = new BuildReconciliationOrderDetailMessage();
            message.setOrderId(trace.getOrderId());
            message.setOrderStatus(convertOrderStatus(trace.getToStatus()));
            return message;
        }).collect(Collectors.toList());
    }




    /**
     * 转换订单状态为对账订单状态
     */
    private ReconciliationOrderStatusEnum convertOrderStatus(OrderStatusEnum orderStatus) {
        if (orderStatus == null) {
            return ReconciliationOrderStatusEnum.PENDING_OUTBOUND;
        }
        
        switch (orderStatus) {
            case WAIT_PAY:
                return ReconciliationOrderStatusEnum.PENDING_OUTBOUND;
            case OUTBOUNDED:
                return ReconciliationOrderStatusEnum.OUTBOUNDED;
            case CANCELLED:
                return ReconciliationOrderStatusEnum.CANCELLED;
            default:
                return ReconciliationOrderStatusEnum.PENDING_OUTBOUND;
        }
    }

}
