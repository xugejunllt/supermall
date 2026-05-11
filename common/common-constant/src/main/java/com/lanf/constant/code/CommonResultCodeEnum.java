package com.lanf.constant.code;

import lombok.Getter;

/**
 * 统一返回结果状态信息类
 */
@Getter
public enum CommonResultCodeEnum {

    SUCCESS(200, "成功"),
    
    FAIL(201, "失败"),

    SERVICE_ERROR(202, "服务异常"),

    SESSION_EXPIRED(300, "token过期"),

    OUT_LOGIN(301, "退出登入"),

    TOKEN_EXPIRED(1001, "Token已过期"),

    AUTH_FAILED(1002, "认证失败"),

    KICKED_OUT(1004, "请重新登录"),
    SIGN_VERIFY_FAILED(40001, "签名验证失败"),
        ;
    private Integer code;

    private String message;

    private CommonResultCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }



}
