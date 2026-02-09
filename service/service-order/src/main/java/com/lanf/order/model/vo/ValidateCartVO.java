package com.lanf.order.model.vo;

import com.lanf.goods.model.vo.ShopGoodsVO;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ValidateCartVO implements Serializable {


    //优惠金额
    private BigDecimal discountPrice;
    //总金额
    private BigDecimal totalPrice;
    //实际支付金额
    private  BigDecimal actualPrice;

    private List<ShopGoodsVO> goodsVOList;

}
