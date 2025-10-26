package com.lanf.rocketmq.model.message;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class LogisticsTrackAddDTO implements Serializable {

    private Long orderId;
    @ApiModelProperty(value = "快递单号")
    private String number;
    @ApiModelProperty(value = "物流状态")
    private Integer status;

    @ApiModelProperty(value = "当前完成时间")
    private Date finishTime;

    @ApiModelProperty(value = "完成内容")
    private String finishContent;


}
