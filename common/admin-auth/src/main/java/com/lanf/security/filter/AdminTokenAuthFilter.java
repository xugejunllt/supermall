//package com.lanf.security.filter;
//
//import com.lanf.web.auth.AuthService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.core.Ordered;
//import org.springframework.stereotype.Component;
//
//import javax.servlet.*;
//import java.io.IOException;
//
///**
// * 用户认证过滤器
// */
//@Slf4j
//@Component
//public class AdminTokenAuthFilter implements Filter, Ordered {
//
//    @Autowired
//    private AuthService authService;
//
//
//    @Override
//    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
//            throws IOException, ServletException {
//        authService.authenticate(servletRequest, servletResponse, filterChain, true);
//    }
//
//
//    @Override
//    public int getOrder() {
//
//        return 1;
//    }
//}
