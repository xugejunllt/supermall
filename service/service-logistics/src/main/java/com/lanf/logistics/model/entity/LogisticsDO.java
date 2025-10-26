package com.lanf.logistics.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 物流信息
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-07-25
 */
@Data
@TableName("logistics")

public class LogisticsDO extends BaseEntity {

private static final long serialVersionUID=1L;


    @ApiModelProperty(value = "订单id")
    private Long orderId;

    private Long userId;

    @ApiModelProperty(value = "快递公司名称")
    private String expressName;

    @ApiModelProperty(value = "快递单号")
    private String number;

    @ApiModelProperty(value = "收货地址")
    private String toAddress;




}
