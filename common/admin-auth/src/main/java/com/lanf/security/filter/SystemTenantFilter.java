package com.lanf.security.filter;


import com.lanf.constant.utils.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import java.io.IOException;

/**
 * System 模块租户过滤器
 * System 模块需要多租户隔离，统一设置为 false
 */
@Component
@Order(Integer.MAX_VALUE)
@Slf4j
public class SystemTenantFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        // System 模块不需要多租户隔离，设置为 false
        TenantContextHolder.setSkipTenant(false);

        try {
            chain.doFilter(request, response);
        } finally {
            // 清理 ThreadLocal，防止内存泄漏
            TenantContextHolder.clear();
            log.debug("[System租户过滤] 清理租户上下文");
        }
    }


}
