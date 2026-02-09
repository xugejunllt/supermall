package com.lanf.goods.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ValidateCartItemVO implements Serializable {
    //总价
    private BigDecimal totalPrice;
    private List<ShopGoodsVO> goodsVOList;

}
