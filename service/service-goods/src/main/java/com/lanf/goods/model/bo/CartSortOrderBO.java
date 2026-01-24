package com.lanf.goods.model.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class CartSortOrderBO implements Serializable {

    private Long userId;
    private Long shopId;

    private Long sortOrder;
}
