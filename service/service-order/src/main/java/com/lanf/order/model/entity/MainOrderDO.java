package com.lanf.order.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * <p>
 * 主订单
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-13
 */
@Data
@TableName("main_order")
public class MainOrderDO extends BaseEntity {

private static final long serialVersionUID=1L;



    @ApiModelProperty(value = "订单编号")
    private String orderNumber;





}
