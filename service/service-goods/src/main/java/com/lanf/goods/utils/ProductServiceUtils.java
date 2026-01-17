package com.lanf.goods.utils;

import com.lanf.common.utils.BigDecimalUtil;

import java.math.BigDecimal;

public class ProductServiceUtils {

    /**
     * 计算订单总金额
     *
     * @param price 单价
     * @param quantity 数量
     * @return 总金额
     */
    public static BigDecimal calculateTotalAmount(BigDecimal price, Integer quantity) {
        return BigDecimalUtil.multiply(price, BigDecimal.valueOf(quantity));
    }
}
