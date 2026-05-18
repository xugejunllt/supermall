package com.lanf.order.model.dto;

import com.lanf.api.order.model.dto.OrderItemDTO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class OrderDTO implements Serializable {

    private Long id;

    private Long shopId;

    /**
     * 商家id
     */
    private Long businessId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 收货地址,json串
     */
    private String takeAddress;

    private List<OrderItemDTO> orderItemDTOList;
}
