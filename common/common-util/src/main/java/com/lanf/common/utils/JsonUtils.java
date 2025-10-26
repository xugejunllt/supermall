package com.lanf.common.utils;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lanf.common.exception.UtilException;

import java.util.List;

public class JsonUtils {
    public static String toJsonString(Object object) {

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            e.printStackTrace();
            throw new UtilException("序列化异常");
        }
    }

    public static <T> T toObject(String json, Class<T> tClass) {

        ObjectMapper objectMapper = new ObjectMapper();
        T value = null;
        try {
            value = objectMapper.readValue(json, tClass);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new UtilException("反序列化异常");
        }

        return value;
    }

    public static <T> List<T> toList(String json, Class<T> tClass) {

        return JSON.parseArray(json, tClass);

    }

}
