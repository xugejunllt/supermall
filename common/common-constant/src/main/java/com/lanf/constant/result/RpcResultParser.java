package com.lanf.constant.result;

import com.lanf.constant.code.CommonResultCodeEnum;
import com.lanf.constant.exception.BizException;

import java.util.Objects;

public class RpcResultParser {


    public static <T>T parseResult(Result<T> result) {


        if (!Objects.equals(result.getCode(), CommonResultCodeEnum.SUCCESS.getCode())) {
           throw new BizException(result.getCode(), result.getMessage());
        }
        return result.getData();
    }
}
