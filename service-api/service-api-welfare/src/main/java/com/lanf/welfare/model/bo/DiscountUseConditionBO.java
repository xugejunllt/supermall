package com.lanf.welfare.model.bo;

import lombok.Data;

/**
 * 打折优惠卷使用条件
 *
 */
@Data
public class DiscountUseConditionBO implements java.io.Serializable{

    //满足金额
    private Double fullMoney;
    //打折数
    private Double discountAmount;
    

}
