package com.lanf.web.interceptor;

import com.lanf.constant.code.CommonCodeEnum;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.result.Result;
import feign.FeignException;
import feign.Response;
import feign.codec.DecodeException;
import feign.codec.Decoder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * 自定义 Feign 解码器
 * 将 Result.fail()
 * <p>
 * 自定义异常抛出
 */
@Slf4j
public class FeignResultDecoder implements Decoder {

    private final Decoder delegate;

    public FeignResultDecoder(Decoder delegate) {
        this.delegate = delegate;
    }

    @Override
    public Object decode(Response response, Type type) throws IOException, DecodeException, FeignException {

        // 先走默认解码
        Object result = delegate.decode(response, type);

//        // 判断 Result
        if (result instanceof Result) {

            Result r = (Result) result;
            if (Objects.equals(r.getCode(), CommonCodeEnum.FEIGN_DEGRADE.getCode())) {

                throw new BizException("下游服务异常");
            }
        }

        return result;
    }
}
