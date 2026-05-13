package com.lanf.goods.model.vo;

import com.lanf.goods.model.bo.ShopGoods;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 清空购物车VO
 */
@Data
public class ClearCartVO implements java.io.Serializable {

    /** 总价 */
    private BigDecimal totalPrice;

    private List<ShopGoods> goodsVOList;

}
