package com.lanf.api.order.model.query;

import lombok.Data;

import java.io.Serializable;

@Data
public class OrderDocumentQuery implements Serializable {

    private Long orderId;

    private Long userId;

}
