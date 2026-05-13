package com.lanf.auth.user.filter;

import com.lanf.constant.code.CommonCodeEnum;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.result.Result;
import com.lanf.constant.utils.TenantContextHolder;
import com.lanf.constant.utils.UserContext;
import com.lanf.web.auth.AuthService;
import com.lanf.web.security.sign.SigningKeyContext;
import com.lanf.web.utils.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
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

        HttpServletResponse response = (HttpServletResponse) servletResponse;
        try {
            authService.authenticate(servletRequest, false);
            filterChain.doFilter(servletRequest, response);
        } catch (BizException e) {
            log.error("用户认证过滤器异常", e);
            ResponseUtil.outFail(response, Result.fail(e.getCode(), e.getMessage()));
        } catch (Exception e) {
            log.error("用户认证过滤器异常", e);
            ResponseUtil.outFail(response, Result.fail(CommonCodeEnum.AUTH_FAILED.getCode(),
                    CommonCodeEnum.AUTH_FAILED.getMessage()));
        } finally {
            UserContext.clear();
            SigningKeyContext.clear();
            TenantContextHolder.clear();
        }
    }
}
