package com.lanf.goods.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ChangeCartQuantityDTO implements Serializable {

    private Long id;
    //变更的数量
    private Integer  changeQuantity;

}
