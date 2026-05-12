package com.lanf.security.filter;

import com.lanf.web.auth.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;

import javax.servlet.*;
import java.io.IOException;

/**
 * 用户认证过滤器
 */
@Slf4j

public class AdminAuthFilter implements Filter, Ordered {


    private AuthService authService;

    public AdminAuthFilter(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        authService.authenticate(servletRequest, servletResponse, filterChain, true);
    }


    @Override
    public int getOrder() {

        return 1;
    }
}
