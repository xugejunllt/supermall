package com.lanf.welfare.model.vo;

import com.lanf.welfare.model.bo.DiscountInfoBO;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class UseMultipleCouponVO implements Serializable {

    //优惠总金额
    private BigDecimal totalDiscountAmount;
    //折扣优惠券信息
    private List<DiscountInfoBO> discountInfoBOList;
}
