package com.lanf.finance.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class ContrastBillVO implements Serializable {

    private Long id;
    private Long orderId;

    @ApiModelProperty(value = "0:支付订单,1:履约单")
    private Integer scanOrderType;

    @ApiModelProperty(value = "0:对账成功,1:对账失败")
    private Integer status;

    @ApiModelProperty(value = "订单状态,0:支付成功,1:取消退款,2:未支付")
    private Integer orderStatus;

    private Date createTime;

    private List<ContrastBillTrackVO> contrastBillTrackVOList;
}
