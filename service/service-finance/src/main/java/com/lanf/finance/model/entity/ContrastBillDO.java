package com.lanf.finance.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-09-01
 */
@Data
@TableName("contrast_bill")
public class ContrastBillDO extends BaseEntity {

private static final long serialVersionUID=1L;


    private Long orderId;

    @ApiModelProperty(value = "0:支付订单,1:履约单")
    private Integer scanOrderType;

    @ApiModelProperty(value = "0:对账成功,1:对账失败")
    private Integer status;

    @ApiModelProperty(value = "订单状态,0:支付成功,1:取消退款,2:未支付")
    private Integer orderStatus;

    private Long shopId;


}
