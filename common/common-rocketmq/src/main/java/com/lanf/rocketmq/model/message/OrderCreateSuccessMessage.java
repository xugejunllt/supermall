package com.lanf.rocketmq.model.message;

import lombok.Data;

import java.io.Serializable;

@Data
public class OrderCreateSuccessMessage implements Serializable {

    private Long orderId;


}
