package com.lanf.rocketmq.model.message;

import com.lanf.constant.mq.base.BaseMessage;
import lombok.Data;

@Data
public class CancelExpiredOrderMessage extends BaseMessage {

    private Long orderId;

    private Long userId;



}
