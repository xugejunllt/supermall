//package com.lanf.pay.mq;
//
//import com.lanf.common.utils.BeanCopyUtils;
//import com.lanf.common.utils.JsonUtils;
//import com.lanf.pay.model.bo.RefundBO;
//import com.lanf.rocketmq.model.TopicName;
//import com.lanf.rocketmq.model.message.RefundDTO;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
//import org.apache.rocketmq.spring.core.RocketMQListener;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//
//@Slf4j
//@Component
//@RocketMQMessageListener(topic = TopicName.REFUND_TOPIC, consumerGroup = TopicName.REFUND_PAY_GROUP)
//public class RefundListener implements RocketMQListener<String> {
//
//    @Autowired
//    private PayServiceAdapter payServiceAdapter;
//
//    /**
//     * 售后退款
//     */
//    @Override
//    public void onMessage(String message) {
//
//        log.info("退款，监听mq消息:{}", message);
//        RefundDTO refundDTO = JsonUtils.toObject(message, RefundDTO.class);
//        try {
//            payServiceAdapter.refund(BeanCopyUtils.copyBean(refundDTO, RefundBO.class));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//}