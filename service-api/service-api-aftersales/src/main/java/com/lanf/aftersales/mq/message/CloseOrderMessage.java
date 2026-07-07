package com.lanf.aftersales.mq.message;

import com.lanf.constant.mq.base.BaseMessage;
import lombok.Data;

@Data
public class CloseOrderMessage extends BaseMessage {

    private Long orderId;
    private Long userId;

}
