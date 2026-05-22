package com.lanf.order.model.bo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class BathAddShippingTrackBO implements Serializable {

    private Long orderId;
    private Long userId;

    private Long tenantId;

    private List<AddShippingTrackBO> shippingTrackBOList;


}
