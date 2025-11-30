package com.lanf.security.utils;

import com.lanf.common.utils.JwtUtils;
import com.lanf.constant.constant.Constants;
import com.lanf.security.model.RefreshTokenTokenBO;
import com.lanf.constant.exception.BizException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.lanf.security.code.SystemResultCodeEnum.TOKENEXPIRED;

@Component
public class TokenUtils {
    @Autowired
    private RedisTemplate redisTemplate;

    public RefreshTokenTokenBO refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String token = request.getHeader(Constants.USER_TOKEN);
        String refreshToken = request.getHeader(Constants.REFRESH_TOKEN);

        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(refreshToken)) {
            throw new BizException(TOKENEXPIRED.getCode(), TOKENEXPIRED.getMessage());
        }

        String tenantCode = JwtUtils.getUsername(token);
        String username = JwtUtils.getTenantCode(token);
        Long userId = JwtUtils.getUserId(token);
        //刷新token
        token = JwtUtils.createAdminToken(userId, username, tenantCode);
        refreshToken = JwtUtils.createRefreshToken(userId, username, tenantCode);
        RefreshTokenTokenBO refreshTokenTokenBO = new RefreshTokenTokenBO();
        refreshTokenTokenBO.setToken(token);
        refreshTokenTokenBO.setRefreshToken(refreshToken);
        //
        response.setHeader(Constants.USER_TOKEN, token);
        response.setHeader(Constants.REFRESH_TOKEN, refreshToken);
        response.setHeader("Access-Control-Expose-Headers", "*");
        return refreshTokenTokenBO;
    }

    public RefreshTokenTokenBO refreshClientToken(HttpServletRequest request, HttpServletResponse response) {
        String token = request.getHeader(Constants.USER_TOKEN);
        String refreshToken = request.getHeader(Constants.REFRESH_TOKEN);

        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(refreshToken)) {
            throw new BizException(TOKENEXPIRED.getCode(), TOKENEXPIRED.getMessage());
        }
        Long userId = JwtUtils.getUserId(token);
        //刷新token
        token = JwtUtils.createUserToken(userId);
        refreshToken = JwtUtils.createUserRefreshToken(userId);
        RefreshTokenTokenBO refreshTokenTokenBO = new RefreshTokenTokenBO();
        refreshTokenTokenBO.setToken(token);
        refreshTokenTokenBO.setRefreshToken(refreshToken);
        //
        response.setHeader(Constants.USER_TOKEN, token);
        response.setHeader(Constants.REFRESH_TOKEN, refreshToken);
        response.setHeader("Access-Control-Expose-Headers", "*");
        return refreshTokenTokenBO;
    }


}
