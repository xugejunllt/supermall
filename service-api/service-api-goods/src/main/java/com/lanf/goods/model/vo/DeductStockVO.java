package com.lanf.goods.model.vo;

import com.lanf.goods.model.bo.GoodsSkuBO;
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
    private GoodsSkuBO goodsSkuBO;

}
