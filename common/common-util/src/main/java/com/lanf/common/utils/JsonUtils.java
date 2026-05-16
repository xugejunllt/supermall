package com.lanf.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lanf.constant.exception.UtilException;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;

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
    /**
     * 【新增】将 JSON 字符串转换为 List<Map<String, String>>
     * 专门用于处理复杂的嵌套结构或动态 Key-Value 场景
     *
     * @param json JSON 字符串
     * @return List<Map<String, String>>
     */
    public static List<Map<String, String>> toMapList(String json) {

        try {
            // 使用 TypeReference 来处理泛型擦除问题
            return objectMapper.readValue(json, new TypeReference<List<Map<String, String>>>() {});
        } catch (Exception e) {
            log.error("JSON 转 List<Map> 失败: {}", json, e);
            return Collections.emptyList();
        }
    }
}
