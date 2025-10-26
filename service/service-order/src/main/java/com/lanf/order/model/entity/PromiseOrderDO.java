package com.lanf.order.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 履约单
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-08-29
 */
@Data
@TableName("promise_order")
public class PromiseOrderDO extends BaseEntity {

private static final long serialVersionUID=1L;


    private Long orderId;

    @ApiModelProperty(value = "履约状态:0:未履约, 1:履约完成")
    private Integer status;

    @ApiModelProperty(value = "退款状态：0:未退款,1:已退款")
    private Integer returnMoney;

    @ApiModelProperty(value = "履约完成时间")
    private Date finishTime;
    //结算状态 0:待结算，1:已结算
    private Integer liquidationStatus;


}
