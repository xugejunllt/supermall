package com.lanf.rocketmq.model.message;

import com.lanf.messagemanager.client.base.BaseMessage;
import lombok.Data;

@Data
public class PrePayMsg  extends BaseMessage {



    private Long orderId;


}
