package com.lanf.pay.mq.listener;

import com.lanf.pay.model.bo.BillDownloadUrlResultBO;
import com.lanf.pay.mq.constant.PayMqGroupName;
import com.lanf.pay.mq.constant.PayMqTopicName;
import com.lanf.pay.mq.message.BillSynchronizerMessage;
import com.lanf.pay.service.pay.PaymentService;
import com.lanf.pay.service.pay.PaymentServiceFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 *
 * 同步账单
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = PayMqTopicName.BILL_SYNCHRONIZER_TOPIC,
        consumerGroup = PayMqGroupName.BILL_SYNCHRONIZER_GROUP
)
public class BillSynchronizerListener implements RocketMQListener<BillSynchronizerMessage> {


    @Override
    public void onMessage(BillSynchronizerMessage message) {

        Integer code = message.getPayChannel().getCode();
        PaymentService paymentService = PaymentServiceFactory.getPaymentService(code);
        //1.获取下载账单下载地址
        BillDownloadUrlResultBO billDownloadUrlResultBO = paymentService.queryBillDownloadUrl(message.getBillType(), message.getBillDate());
        //2.下载账单并存储到 FundBillDetail


    }

}
