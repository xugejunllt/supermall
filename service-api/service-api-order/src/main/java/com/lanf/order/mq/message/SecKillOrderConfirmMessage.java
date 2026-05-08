package com.lanf.order.mq.message;

import com.lanf.order.model.enums.OrderProcessStepEnum;
import lombok.Data;

import java.io.Serializable;

@Data
public class SecKillOrderConfirmMessage implements Serializable {

    private String orderNumber;

    private OrderProcessStepEnum orderProcessStep;

}
