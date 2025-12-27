package com.lanf.rocketmq.model.message;

import com.lanf.messagemanager.client.model.base.BaseMqMessage;
import lombok.Data;

@Data
public class DeductCouponTemplateCountMsg extends BaseMqMessage {

    //优惠卷模板id
    private Long couponTemplateId;
    //扣减数量
    private Integer deductCount;
}
