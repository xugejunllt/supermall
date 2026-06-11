package com.lanf.constant.code;

import lombok.Getter;

/**
 * 统一返回结果状态信息类
 */
@Getter
public enum CommonCodeEnum {

    SUCCESS(200, "成功"),
    
    FAIL(201, "服务繁忙,请稍后再试"),

    /**
     * 客户端重定向 token刷新接口
     */
    TOKEN_EXPIRED(1001, "Token已过期"),
    /**
     * 客户端执行退出登入
     */
    KICKED_OUT(1002, "请重新登录"),

    AUTH_FAILED(1003, "认证失败"),

    SIGN_VERIFY_FAILED(1005, "签名验证失败"),

    /**
     * Feign 调用专属异常
     */
    FEIGN_DEGRADE(1006, "服务繁忙,请稍后再试"),
        ;
    private final Integer code;

    private final String message;

    private CommonCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }



}
