package com.lanf.goods.model.vo;

import com.lanf.goods.model.bo.ShopGoods;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 验证购物车项VO
 */
@Data
public class ValidateCartItemVO implements Serializable {
    
    /** 总价 */
    private BigDecimal totalPrice;
    
    private List<ShopGoods> goodsVOList;

}
