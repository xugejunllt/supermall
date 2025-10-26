package com.lanf.logistics.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class LogisticsTrackVO implements Serializable {

    @ApiModelProperty(value = "当前完成时间")
    private Date finishTime;

    @ApiModelProperty(value = "完成内容")
    private String finishContent;

    private Integer status;

    private String statusName;
}
