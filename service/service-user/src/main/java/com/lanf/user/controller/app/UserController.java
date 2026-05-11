package com.lanf.user.controller.app;


import com.lanf.common.utils.JsonUtils;
import com.lanf.user.model.dto.LoginUserDTO;
import com.lanf.user.model.dto.RefreshTokenDTO;
import com.lanf.user.model.dto.RegisterUserDTO;
import com.lanf.user.model.vo.*;
import com.lanf.user.service.IUserService;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.result.Result;
import com.lanf.web.security.keygen.PublicKeyManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;


@Slf4j
@RestController
@RequestMapping("/app/user")
public class UserController {

    @Autowired
    private IUserService userService;

    @Autowired
    private PublicKeyManager publicKeyManager;


    @PostMapping("/register")
    public Result<Void> register(@Validated @RequestBody RegisterUserDTO dto) {

        log.info("[{}]开始,入参:[{}]", "注册", JsonUtils.toJsonString(dto));
        userService.registerUser(dto);
        log.info("[{}]结束", "注册");

        return Result.ok();
    }

    @PostMapping("/registerSendCode")
    public Result<Void> registerSendCode(@NotNull(message = "手机号不能为空") String phoneNumber) {

        log.info("[{}]开始,入参:[{}]", "注册发送短信验证码", phoneNumber);

        userService.registerSendCode(phoneNumber);

        log.info("[{}]结束", "注册发送短信验证码");

        return Result.ok();
    }

    @PostMapping("/login")
    public Result<UserTokenInfoVO> login(@Validated @RequestBody LoginUserDTO dt, HttpServletRequest request) {

        log.info("登入开始,入参:[{}]" , dt);

        UserTokenInfoVO userVO = userService.login(dt,request);

        return Result.ok(userVO);
    }

    @PostMapping("/loginSendCode")
    public Result<Void> loginSendCode(@NotNull(message = "手机号不能为空") String phoneNumber) {

        log.info("[{}]开始,入参:[{}]", "登入发送短信验证码", phoneNumber);

        userService.loginSendCode(phoneNumber);

        log.info("[{}]结束", "登入发送短信验证码");

        return Result.ok();
    }

    /**
     * 如果出现异常 统一退出登入
     * @param dto
     * @return
     */
    @PostMapping("/refreshToken")
    public Result<UserTokenInfoVO> refreshToken(@Validated @RequestBody RefreshTokenDTO dto,HttpServletRequest request) {

        log.info("[{}]开始,入参:[{}]", "刷新token", dto);

        UserTokenInfoVO refreshTokenVO = userService.refreshToken(dto,request);

        log.info("[{}]结束", "刷新token");

        return Result.ok(refreshTokenVO);
    }

    @GetMapping("/test")
    public Result<Void> test() {

        log.info("[{}]开始,入参");

        log.info("[{}]结束", "刷新token");
        throw new BizException("1");

    }

    @GetMapping("/getUserById")
    public Result<UserVO> getUserById() {

        log.info("[{}]开始","根据id查询用户信息");

        return Result.ok(userService.getUserById());
    }

    @GetMapping("/getUserDetail")
    public Result<UserDetailVO> getUserDetail() {

        log.info("[{}]开始", "获取用户详细");

        return Result.ok(userService.getUserDetail());
    }

    /**
     * 获取公钥
     * @return 公钥信息
     */
    @GetMapping("/getPublicKey")
    public Result<PublicKeyVO> getPublicKey() {

        log.info("[{}]开始", "获取公钥");

        PublicKeyManager.PublicKeyInfo publicKeyInfo = publicKeyManager.generatePublicKey();
        
        PublicKeyVO publicKeyVO = new PublicKeyVO();
        publicKeyVO.setRandomKey(publicKeyInfo.getRandomKey());
        publicKeyVO.setPublicKey(publicKeyInfo.getPublicKey());

        log.info("[{}]结束,randomKey:[{}]", "获取公钥", publicKeyInfo.getRandomKey());

        return Result.ok(publicKeyVO);
    }
}

