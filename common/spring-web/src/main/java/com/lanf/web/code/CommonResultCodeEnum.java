package com.lanf.web.code;

import com.lanf.web.exception.BizException;
import lombok.Getter;

/**
 * 统一返回结果状态信息类
 */
@Getter
public enum CommonResultCodeEnum {

    SUCCESS(200, "成功"),
    /**
     * 通用错误code 业务主动抛出异常 通常用于业务校验
     * message通常会被重置
     */
    FAIL(201, "失败"),

    /**
     * 发生exception code
     */
    SERVICE_ERROR(202, "服务异常"),


    /**
     * 特殊业务处理code 通常前段需求处理的code
     */
    SESSION_EXPIRED(300, "token过期"),

    OUT_LOGIN(301, "退出登入");

    //MethodArgumentNotValid
//    LOGIN_AUTH(208, "请先登录"),
//    PERMISSION(209, "没有权限"),
//    CACHEEXPIRED(210, "缓存过期"),
//    TOKENEXPIRED(50014, "登录过期,请重新登录"),
//    TOKENEXPIREDBYMENU(300, "登录过期,请重新登录"),
//    ACCOUNT_ERROR(214, "账号不正确"),
//    PASSWORD_ERROR(215, "密码不正确"),
//    LOGIN_MOBLE_ERROR(216, "账号或密码错误"),
//    ACCOUNT_STOP(217, "账号已停用");




    private Integer code;

    private String message;

    private CommonResultCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }



}
