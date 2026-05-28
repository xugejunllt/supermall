package com.lanf.welfare.mq.message;

import com.lanf.constant.mq.base.BaseMessage;
import lombok.Data;

@Data
public class DeductCouponTemplateCountMessage extends BaseMessage {

    private Integer deductCount;


    private Long couponTemplateId;

}
