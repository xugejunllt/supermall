package com.lanf.web.code;

import com.lanf.web.exception.BizException;
import lombok.Getter;

/**
 * 统一返回结果状态信息类
 */
@Getter
public enum CommonResultCodeEnum {

    SUCCESS(200, "成功"),
    FAIL(201, "失败"),
    SERVICE_ERROR(202, "服务异常"),
    DATA_ERROR(203, "数据异常"),
    ILLEGAL_REQUEST(204, "非法请求"),
    REPEAT_SUBMIT(205, "重复提交"),
    ARGUMENT_VALID_ERROR(210, "参数校验异常"),
    DATA_NOT_FOUNT(211, "数据不存在");
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
    public static  BizException throwDataNotFountException(String message){

        throw new BizException(CommonResultCodeEnum.DATA_NOT_FOUNT.getCode(), message);
    }


}
