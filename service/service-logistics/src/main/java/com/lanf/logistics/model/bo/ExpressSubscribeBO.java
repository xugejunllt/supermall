package com.lanf.logistics.model.bo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class ExpressSubscribeBO implements Serializable {

    @ApiModelProperty(value = "快递单号")
    private String number;

    @ApiModelProperty(value = "快递公司编码")
    private String companyNumber;
}
