package com.lanf.user.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class LoginUserVO implements Serializable {

    private Long userId;

    private String token;




}
