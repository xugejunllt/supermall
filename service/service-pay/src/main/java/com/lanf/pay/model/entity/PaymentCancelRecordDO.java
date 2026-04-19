package com.lanf.pay.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 三方支付订单取消记录
 * </p>
 *
 * @author jarven
 * @since 2026-04-19
 */
@Data
@TableName("payment_cancel_record")
public class PaymentCancelRecordDO extends BaseEntity {

private static final long serialVersionUID=1L;



    private String outTradeNo;

    @ApiModelProperty(value = "支付类型")
    private Integer payType;

    @ApiModelProperty(value = "三方支付订单当前状态 0:未发起交易,1:待支付 ,2: 已支付，进行退款")
    private Integer currentPayStatus;

    @ApiModelProperty(value = "取消订单的来源：0:用户手动取消 ,1：系统定时任务超时取消")
    private Integer cancelSource;




}
