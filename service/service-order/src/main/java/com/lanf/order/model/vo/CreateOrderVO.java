package com.lanf.order.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateOrderVO implements Serializable {



    private Long mainOrderId;

    private Long orderId;

    private List<Long> orderIdList;

}
