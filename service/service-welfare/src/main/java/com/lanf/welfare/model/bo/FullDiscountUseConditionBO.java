package com.lanf.welfare.model.bo;

import lombok.Data;

/**
 * 满减优惠卷使用条件
 *
 */
@Data
public class FullDiscountUseConditionBO implements java.io.Serializable{

    //满足金额
    private Double fullMoney;
    //优惠金额
    private Double discountMoney;


}
