package com.lanf.seckill.mq.message;

import com.lanf.constant.mq.base.BaseMessage;
import lombok.Data;

@Data
public class SecKillSuccessMessage extends BaseMessage {

    private Long userId;

    private Long secKillItemId;

    private String orderNumber;

    private Integer itemQuantity;

}
