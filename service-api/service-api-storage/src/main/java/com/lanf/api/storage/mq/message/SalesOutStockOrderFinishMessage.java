package com.lanf.api.storage.mq.message;

import com.lanf.constant.mq.base.BaseMessage;
import lombok.Data;

@Data
public class SalesOutStockOrderFinishMessage extends BaseMessage {

    private Long orderId;

    private Long userId;

}
