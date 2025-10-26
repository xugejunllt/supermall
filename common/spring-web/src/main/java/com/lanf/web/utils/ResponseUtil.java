package com.lanf.web.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lanf.web.result.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class ResponseUtil {

    public static void out(HttpServletResponse response, Result r) {
        ObjectMapper mapper = new ObjectMapper();
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        try {
            mapper.writeValue(response.getWriter(), r);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void out(HttpServletResponse response, String result) {
        PrintWriter out = null;
        try {
            out = response.getWriter();
            out.print(result);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            if (out != null){
                out.close();
            }
        }


    }
}
