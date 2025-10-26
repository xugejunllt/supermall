package com.lanf.security.code;

import lombok.Getter;

/**
 * 统一返回结果状态信息类
 */
@Getter
public enum SystemResultCodeEnum {


    TOKENEXPIRED(1001, "登录过期"),
    LOGIN_AUTH(1002, "未登陆"),
    PERMISSION(1003, "没有权限"),
    LOGIN_MOBLE_ERROR(1004,"登入失败"),
    REFRESH_TOKEN(1005,"token刷新");
    private Integer code;

    private String message;

    private SystemResultCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
