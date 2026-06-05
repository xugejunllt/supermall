package com.lanf.pay.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@TableName("prepay_pay_type")
public class PrepayPayTypeDO extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "交易订单号，与三方支付单唯一关联号")
    private String outTradeNo;


    private Integer payType;
    /**
     * 预支付订单信息
     */
    private String orderStr;
}
