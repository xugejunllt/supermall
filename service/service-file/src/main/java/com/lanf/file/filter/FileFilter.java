package com.lanf.file.filter;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Slf4j
@Component
public class FileFilter implements Filter {



    @Override
    public void doFilter(ServletRequest request, ServletResponse response1, FilterChain chain) throws ServletException, IOException {

        HttpServletRequest request1 = (HttpServletRequest) request;
        log.info("请求路径[{}]", request1.getRequestURI());

        chain.doFilter(request, response1);
    }



}
