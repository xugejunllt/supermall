package com.lanf.pay.model.bo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class PayCompensateOrderRetryPolicyBO implements Serializable {

    @ApiModelProperty(value = "重试次数（第几次重试）")
    private Integer retryLevel;

    @ApiModelProperty(value = "延迟秒数（从上次失败开始）")
    private Integer delaySeconds;





}
