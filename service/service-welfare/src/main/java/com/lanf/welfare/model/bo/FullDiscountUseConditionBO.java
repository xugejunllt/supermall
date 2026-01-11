package com.lanf.welfare.model.bo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 满减优惠卷使用条件
 *
 */
@Data
public class FullDiscountUseConditionBO implements java.io.Serializable{

    //满足金额
    private BigDecimal fullMoney;
    //优惠金额
    private BigDecimal discountMoney;


}
