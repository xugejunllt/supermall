package com.lanf.system.service;

import com.lanf.system.model.dto.RefreshTokenDTO;
import com.lanf.system.model.vo.AdminTokenInfoVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 管理员认证服务接口
 */
public interface IAdminAuthService {

    /**
     * 刷新 Token
     * 
     * @param dto 刷新令牌请求
     * @param request HTTP 请求
     * @return 新的 Token 信息
     */
    AdminTokenInfoVO refreshToken(RefreshTokenDTO dto, HttpServletRequest request);
}
