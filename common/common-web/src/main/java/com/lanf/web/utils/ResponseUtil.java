package com.lanf.web.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lanf.constant.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class ResponseUtil {

    public static void outFail(HttpServletResponse response, Result<?> r) {
        ObjectMapper mapper = new ObjectMapper();
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        try {
            mapper.writeValue(response.getWriter(), r);
        } catch (IOException e) {
            log.error("响应结果错误", e);
        }
    }
    public static void outSuccess(HttpServletResponse response, Result<?> r) {
        ObjectMapper mapper = new ObjectMapper();
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        try {
            mapper.writeValue(response.getWriter(), r);
        } catch (IOException e) {
            log.error("响应结果错误", e);
        }
    }
    public static void outSuccess(HttpServletResponse response, Object object) {
        ObjectMapper mapper = new ObjectMapper();
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        try {
            mapper.writeValue(response.getWriter(),object);
        } catch (IOException e) {
            log.error("响应结果错误", e);
        }
    }

}
