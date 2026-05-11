package com.lanf.user.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;

import java.io.Serializable;

@Data
public class AddBenefitDTO implements Serializable {


    @ApiModelProperty(value = "权益code")
    @NotBlank( message = "权益code不能为空")
    private String code;

    @ApiModelProperty(value = "权益名称")
    @NotBlank( message = "权益名称不能为空")
    private String name;

    //权益描述
    @NotBlank( message = "权益描述不能为空")
    private String benefitDesc;


}
