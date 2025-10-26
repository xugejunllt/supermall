package com.lanf.rocketmq.model.message;

import com.lanf.messagemanager.client.base.BaseMessage;
import lombok.Data;

import java.io.Serializable;

@Data
public class PromiseOrderReturnMoneyDTO extends BaseMessage {

    private Long orderId;
}
