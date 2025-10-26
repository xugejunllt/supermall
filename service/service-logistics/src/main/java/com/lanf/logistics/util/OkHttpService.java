package com.lanf.logistics.util;

import com.lanf.web.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class OkHttpService {

    private final OkHttpClient client;

    /**
     *
     *
     * 待优化成线程池 每个业务不同的线程池配置或者使用默认的
     *
     */
    @Autowired
    public OkHttpService(OkHttpClient client) {
        this.client = client;
    }

    /**
     * 发送基本的post请求 :application/x-www-form-urlencoded
     */
    public String postExecute(String url, Map<String, String> parmMap, Map<String, String> headMap) {

        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        Request.Builder builder = new Request.Builder();

        if (parmMap != null) {
            //添加请求参数
            for (Map.Entry<String, String> entry : parmMap.entrySet()) {
                formBodyBuilder.add(entry.getKey(), entry.getValue());
            }

        }
        if (headMap != null) {
            //添加请求头
            for (Map.Entry<String, String> entry : headMap.entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue());
            }
        }
        Request request = builder
                .url(url)
                .post(formBodyBuilder.build())
                .build();

        return sendRequest(request);
    }

    private void okCheck(Response response) {

        int code = response.code();
        if (code != 200) {
            log.error("http3状态码异常,code:{}", code);
            throw new BizException("http3状态码异常");
        }
    }

    private String sendRequest(Request request) {

        Response response = null;

        String result = null;
        try {
            response = client.newCall(request).execute();
            okCheck(response);
            result = response.body().string();
        } catch (IOException e) {
            log.error("http3请求异常", e.fillInStackTrace());
            throw new BizException("http3请求异常");
        }

        return result;
    }

}