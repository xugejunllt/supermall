package com.lanf.order.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CartInfoDTO implements Serializable {

    private Long cartId;

    private Long warehouseId;


}
