package com.lanf.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lanf.constant.exception.UtilException;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class JsonUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();


    public static String toJsonString(Object object) {


        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {

            log.error("序列化异常object[{}],异常信息[{}]",object,StackTraceUtil.getStackTrace(e));

            throw new UtilException("序列化异常");
        }
    }

    public static <T> T toObject(String json, Class<T> tClass) {

        T value = null;
        try {
            value = objectMapper.readValue(json, tClass);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new UtilException("反序列化异常");
        }

        return value;
    }


    // 反序列化 List
    public static <T> List<T> toList(String json, Class<T> elementClass)  {

        JavaType javaType = objectMapper.getTypeFactory()
                .constructCollectionType(List.class, elementClass);
        List<T> object = null;
        try {

            object = objectMapper.readValue(json, javaType);

        } catch (JsonProcessingException e) {

            log.error("反序列化异常json[{}],elementClass[{}],异常信息[{}]",elementClass,json,StackTraceUtil.getStackTrace(e));
            throw new UtilException("反序列化异常");
        }

        return object;



    }
}
