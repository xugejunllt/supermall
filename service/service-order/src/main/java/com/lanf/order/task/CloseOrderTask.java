package com.lanf.order.task;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.PromiseOrderLiquidationMsg;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.Date;

/**
 * 关闭订单任务
 *
 */
@Slf4j
@Component
public class CloseOrderTask {


    @Autowired
    private RocketMqClient rocketMqClient;

    /**
     * 关闭已评价的订单
     *
     */
    @Scheduled(cron = "0/5 * * * * *")
    public void closeReviewedOrder () {

        log.info("履约单定时任务检查开始");
        /**
         * 查询对账任务
         */
        //前一天时间
        Date date = new Date();
        LambdaQueryChainWrapper<PromiseOrderDO> promiseOrderDOLambdaQueryChainWrapper = promiseOrderService.lambdaQuery().
                le(PromiseOrderDO::getFinishTime, date).
                eq(PromiseOrderDO::getStatus, 1).
                eq(PromiseOrderDO::getReturnMoney, 0).
                eq(PromiseOrderDO::getLiquidationStatus, 0);
        //履约时间到期的订单
        double count = promiseOrderDOLambdaQueryChainWrapper.count().doubleValue();
        if (count < 1){
            log.info("没有找到符合条件的履约单");
            return;
        }
        double pageSize = 200;
        //向上取整
        double currentTotal = Math.ceil(count / pageSize);
        long currentTotal2 = (long) currentTotal;

        for (long i = 1; i <= currentTotal2; i++) {

            IPage<PromiseOrderDO> page = new Page<>(i, (long) pageSize);
            List<PromiseOrderDO> records = promiseOrderDOLambdaQueryChainWrapper.
                    page(page).getRecords();
            for (PromiseOrderDO a : records) {

                log.info("履约完成，准备进行结算");
                PromiseOrderLiquidationMsg promiseOrderLiquidationMsg = new PromiseOrderLiquidationMsg();
                promiseOrderLiquidationMsg.setOrderId(a.getOrderId());
                rocketMqClient.sendMessage(TopicName.PROMISE_ORDER_LIQUIDATION_TOPIC, promiseOrderLiquidationMsg);

            }


        }

    }

}
