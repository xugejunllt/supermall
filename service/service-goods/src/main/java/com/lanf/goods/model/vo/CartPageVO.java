package com.lanf.goods.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 购物车列表VO
 */
@Data
public class CartPageVO implements Serializable {



    private Long shopId;
    
    /** 店铺名称 */
    private String shopName;

    private List<CartItemVO> cartItemList;

}
