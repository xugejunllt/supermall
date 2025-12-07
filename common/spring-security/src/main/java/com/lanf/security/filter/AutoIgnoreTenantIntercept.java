package com.lanf.security.filter;

import com.lanf.common.utils.AutoIgnoreTenantContext;
import com.lanf.security.config.FilterPathConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

/**
 *
 * 自动忽略掉多租户表的拦截查询
 * app用户操作不需要 为了适配 MybatisPlus 查询
 *
 */
@Slf4j
@Component
public class AutoIgnoreTenantIntercept implements Filter {

    @Autowired
    private FilterPathConfig filterPathConfig;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        List<String> autoIgnoreTenantPath = filterPathConfig.getAutoIgnoreTenantPath();

        HttpServletRequest request1 = (HttpServletRequest) request;

        try {
            AutoIgnoreTenantContext.setAutoIgnoreMark(autoIgnoreTenantPath.
                    contains(request1.getRequestURI()));
            //请求放行
            chain.doFilter(request, response);

        } finally {
            AutoIgnoreTenantContext.removeAutoIgnoreMark();

        }
    }
}
