package com.lanf.pay.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 批量支付订单
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-15
 */
@Data
@TableName("bath_pay_order")
public class BathPayOrderDO extends BaseEntity {

private static final long serialVersionUID=1L;



    @ApiModelProperty(value = "商家id")
    private Long businessId;

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "主订单id")
    private Long mainOrderId;

    @ApiModelProperty(value = "批量付款批次号")
    private String batchNo;

    @ApiModelProperty(value = "付款总笔数")
    private Integer batchNum;

    @ApiModelProperty(value = "付款总金额")
    private BigDecimal batchFee;
    @ApiModelProperty(value = "过期时间")
    private Date expireTime;

    @ApiModelProperty(value = "过期时间间隔（秒）")
    private Integer expireInterval;


}
