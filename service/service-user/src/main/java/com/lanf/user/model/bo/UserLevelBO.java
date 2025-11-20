package com.lanf.user.model.bo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class UserLevelBO implements Serializable {

    //等级
    private Integer level;

    @ApiModelProperty(value = "等级名称，如VIP1")
    private String name;

    @ApiModelProperty(value = "等级图标")
    private String icon;


}
