package com.lanf.security.filter;

import com.lanf.common.utils.BeanUtil;
import com.lanf.common.utils.IStringUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.result.Result;
import com.lanf.constant.utils.TenantContextHolder;
import com.lanf.constant.utils.UserContext;
import com.lanf.security.service.PermissionCacheService;
import com.lanf.web.auth.AuthService;
import com.lanf.web.auth.RequestAuthExtractor;
import com.lanf.web.config.AuthPathConfig;
import com.lanf.web.model.bo.AuthRequestInfo;
import com.lanf.web.security.sign.SigningKeyContext;
import com.lanf.web.utils.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 实现 OncePerRequestFilter接口 加入到security过滤器链中
 */

@Slf4j
public class AdminPermissionFilter extends OncePerRequestFilter implements Ordered {


    private final PermissionCacheService permissionCacheService;
    private final AuthService authService;

    private final AuthPathConfig authPathConfig;

    public AdminPermissionFilter() {
        this.permissionCacheService = BeanUtil.getBean(PermissionCacheService.class);
        this.authService = BeanUtil.getBean(AuthService.class);
        this.authPathConfig = BeanUtil.getBean(AuthPathConfig.class);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            log.info("token鉴权开始");
            authService.authenticate(request, true);
            log.info("token鉴权结束");


            if (authPathConfig.getExcludeAuthPaths().contains(request.getRequestURI())){
                /**
                 * 跳过菜单权限
                 */
                log.info("跳过菜单权限");
                filterChain.doFilter(request, response);
                return;
            }
            log.info("执行菜单权限过滤器开始");
            AuthRequestInfo requestInfo = RequestAuthExtractor.extractBasicInfo(request);

            List<GrantedAuthority> permissions = permissionCacheService.getPermissions(UserContext.getUserId()
                    , requestInfo.getChannel());
            if (IStringUtils.isEmpty(permissions)) {
                log.error("添加菜单权限异常");
                ResponseUtil.outFail(response, Result.fail("添加菜单权限异常"));
            }
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    UserContext.getUserId(),
                    null,
                    permissions
            );
            /**
             * 菜单前权限 添加spring security上下文汇总
             */
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
            log.info("执行菜单权限过滤器结束");
        } catch (BizException e) {
            log.error("鉴权异常", e);
            ResponseUtil.outFail(response, Result.fail(e.getCode(),e.getMessage()));
        } catch (Exception e) {
            log.error("鉴权异常", e);
            ResponseUtil.outFail(response, Result.fail("鉴权异常"));
        } finally {
            UserContext.clear();
            SigningKeyContext.clear();
            TenantContextHolder.clear();
        }
    }

    /**
     * 执行 顺序 后与 AdminTokenAuthFilter
     */
    @Override
    public int getOrder() {
        return 1;
    }
}
