package com.lanf.api.order.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ReconciliationOrderItemVO implements Serializable {

    private String createDate;

    private List<ReconciliationOrderItem> orderItemVOS;
}
