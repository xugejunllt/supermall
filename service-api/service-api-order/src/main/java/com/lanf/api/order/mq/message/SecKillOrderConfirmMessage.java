package com.lanf.api.order.mq.message;

import com.lanf.api.order.model.enums.OrderProcessStepEnum;
import lombok.Data;

import java.io.Serializable;

@Data
public class SecKillOrderConfirmMessage implements Serializable {

    private String orderNumber;

    private OrderProcessStepEnum orderProcessStep;

}
