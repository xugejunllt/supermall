package com.lanf.dynamicsrrefresh.service.result;

import lombok.Data;

/**
 * 全局统一返回结果类
 *
 */
@Data
public class Result<T> {

    //返回码
    private Integer code;

    //返回消息
    private String message;

    //返回数据
    private T data;

    public Result(){}

    // 返回数据


    public static<T> Result<T> ok(){
        return Result.ok(null);
    }

    /**
     * 操作成功
     * @param data  baseCategory1List
     * @param <T>
     * @return
     */
    public static<T> Result<T> ok(T data){
        Result<T> result = new Result<>();
        result.setCode(CommonResultCodeEnum.SUCCESS.getCode());
        result.setMessage(CommonResultCodeEnum.SUCCESS.getMessage());
        result.setData(data);
        return   result;
    }

    public static<T> Result<T> fail(Integer code,String message){

        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return  result;
    }

    public static<T> Result<T> fail(String message){

        Result<T> result = new Result<>();
        result.setCode(CommonResultCodeEnum.FAIL.getCode());
        result.setMessage(message);
        return  result;
    }
    public static<T> Result<T> fail(){

        Result<T> result = new Result<>();
        result.setCode(CommonResultCodeEnum.SERVICE_ERROR.getCode());
        result.setMessage(CommonResultCodeEnum.SERVICE_ERROR.getMessage());
        return  result;
    }


}
