package com.lanf.rocketmq.model.message;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class OrderStatusChangeDTO implements Serializable {



    private List<Long> orderIdList;
    //0:支付成功 1:出库成功
    private Integer event;
}
