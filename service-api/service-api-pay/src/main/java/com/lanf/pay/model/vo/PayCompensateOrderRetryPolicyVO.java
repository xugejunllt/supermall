package com.lanf.pay.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class PayCompensateOrderRetryPolicyVO implements Serializable {

    @ApiModelProperty(value = "重试次数（第几次重试）")
    private Integer retryLevel;

    @ApiModelProperty(value = "延迟秒数（从上次失败开始）")
    private Integer delaySeconds;

    @ApiModelProperty(value = "累计耗时（秒）")
    private Integer accumulatedSeconds;

    @ApiModelProperty(value = "描述，如“5秒后重试”")
    private String description;

    @ApiModelProperty(value = "是否启用0启用,1:禁用")
    private Integer isEnabled;

}
