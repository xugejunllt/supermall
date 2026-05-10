package com.lanf.auth.user.filter;

import com.lanf.web.service.AuthService;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import java.io.IOException;

@Slf4j
public class UserAuthFilter implements Filter {


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        AuthService.authenticate(servletRequest, servletResponse, filterChain,false);
    }


}
