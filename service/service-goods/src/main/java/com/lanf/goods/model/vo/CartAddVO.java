package com.lanf.goods.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class CartAddVO implements Serializable {

    private Long shopId;

    private Long sortOrder;
}
