package com.lanf.user.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Data
public class AddUserLevelConfigDTO implements Serializable {

    @ApiModelProperty(value = "等级- 从1开始递增")
    @NotNull( message = "等级不能为空 ")
    private Integer level;

    @ApiModelProperty(value = "等级名称，如VIP1")
    @NotBlank( message = "等级名称不能为空")
    private String name;

    @ApiModelProperty(value = "等级图标")
    @NotBlank( message = "等级图标不能为空")
    private String icon;

    @ApiModelProperty(value = "该等级成长值")
    @NotNull( message = "该等级成长值不能为空")
    private Integer growthValue;

    @ApiModelProperty(value = "权益列表JSON配置")
    @NotEmpty( message = "权益不能为空")
    private List<LevelBenefitDTO> levelPrivileges;



}
