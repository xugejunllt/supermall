package com.lanf.auth.user.filter;

import com.lanf.web.auth.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import java.io.IOException;

/**
 * 用户认证过滤器
 */
@Slf4j
@Component
@Order(1)
public class UserAuthFilter implements Filter {

    @Autowired
    private AuthService authService;
    
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        authService.authenticate(servletRequest, servletResponse, filterChain, false);
    }
}
