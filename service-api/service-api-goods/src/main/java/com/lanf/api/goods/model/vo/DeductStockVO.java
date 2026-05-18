package com.lanf.api.goods.model.vo;

import com.lanf.api.goods.model.bo.GoodsSku;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 扣减库存VO
 */
@Data
public class DeductStockVO implements Serializable {

    /** 订单总金额 */
    private  BigDecimal totalAmount;



    /** 商品信息 */
    private GoodsSku goodsSkuBO;

}
