// ... existing code ...
// 这是新文件，请直接创建以下完整代码文件：
package com.lanf.order.mq.listener;

import com.lanf.order.model.bo.AddShippingTrackBO;
import com.lanf.order.model.bo.BathAddShippingTrackBO;
import com.lanf.order.mq.constant.OrderMqGroupName;
import com.lanf.order.mq.constant.OrderMqTopicName;
import com.lanf.order.mq.message.BathAddShippingTrackMessage;
import com.lanf.order.mq.message.ShippingTrackMessage;
import com.lanf.order.service.shipping.IShippingTrackService;
import com.lanf.rocketmq.annotation.MqRetryConsume;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RocketMQMessageListener(
        topic = OrderMqTopicName.BATH_ADD_SHIPPING_TRACK_TOPIC,
        consumerGroup = OrderMqGroupName.BATH_ADD_SHIPPING_TRACK_GROUP
)
public class BathAddShippingTrackListener implements RocketMQListener<BathAddShippingTrackMessage> {

    @Autowired
    private IShippingTrackService shippingTrackService;
    @MqRetryConsume(messageId = "#message.messageId")
    @Override
    public void onMessage(BathAddShippingTrackMessage message) {

        log.info("接收到批量添加物流轨迹消息: {}", message);

        BathAddShippingTrackBO bo = new BathAddShippingTrackBO();
        bo.setOrderId(message.getOrderId());
        bo.setUserId(message.getUserId());
        bo.setTenantId(message.getTenantId());

        List<AddShippingTrackBO> trackBOList = new ArrayList<>();

        for (ShippingTrackMessage trackMessage : message.getShippingTrackList()) {
            AddShippingTrackBO trackBO = new AddShippingTrackBO();
            trackBO.setStatus(trackMessage.getStatus());
            trackBO.setBaseTrackStatus(trackMessage.getBaseTrackStatus());
            trackBO.setFinishTime(trackMessage.getFinishTime());
            trackBO.setFinishContent(trackMessage.getFinishContent());
            trackBO.setFlowNo(trackMessage.getFlowNo());
            trackBOList.add(trackBO);
        }

        bo.setShippingTrackBOList(trackBOList);

        shippingTrackService.bathAddShippingTrack(bo);
    }
}
