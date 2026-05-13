package com.lanf.system.controller.system;

import com.lanf.constant.result.Result;
import com.lanf.system.model.dto.RefreshTokenDTO;
import com.lanf.system.model.vo.AdminTokenInfoVO;
import com.lanf.system.service.IAdminAuthService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 管理员认证控制器
 * 提供 Token 刷新等认证相关接口
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@Api(tags = "管理员认证管理")
public class AdminAuthController {

    @Autowired
    private IAdminAuthService adminAuthService;

    /**
     * 刷新 Token
     * 使用 Refresh Token 获取新的 Access Token 和 Refresh Token
     * 
     * @param dto 刷新令牌请求
     * @param request HTTP 请求（用于提取设备ID等信息）
     * @return 新的 Token 信息
     */
    @PostMapping("/refreshToken")
    @ApiOperation("刷新Token")
    public Result<AdminTokenInfoVO> refreshToken(
            @Validated @RequestBody RefreshTokenDTO dto,
            HttpServletRequest request) {
        
        log.info("接收到刷新Token请求");
        
        AdminTokenInfoVO tokenInfo = adminAuthService.refreshToken(dto, request);
        
        return Result.ok(tokenInfo);
    }
}
