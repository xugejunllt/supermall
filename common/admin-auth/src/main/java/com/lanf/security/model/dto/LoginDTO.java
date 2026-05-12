package com.lanf.security.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 登入表单参数
 */
@Data
public class LoginDTO implements Serializable {

    private String username;

    private String password;

    private String tenantCode;

}
