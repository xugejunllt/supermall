package com.lanf.goods.model.bo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class CartBO implements Serializable {


    //店铺id
    private Long shopId;
    //店铺名称
    private String shopName;

    private List<CartGoodsVO> cartGoodsVOList;


}
