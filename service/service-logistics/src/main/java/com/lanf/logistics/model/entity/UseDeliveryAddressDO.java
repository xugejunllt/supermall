package com.lanf.logistics.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 常用发货地址
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-07-25
 */
@Data
@TableName("use_delivery_address")
public class UseDeliveryAddressDO extends BaseEntity {

private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "主健")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "联系人")
    private String consignee;

    @ApiModelProperty(value = "手机号")
    private String phone;

    @ApiModelProperty(value = "地区")
    private String area;

    @ApiModelProperty(value = "地址")
    private String address;




}
