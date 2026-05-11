package com.lanf.user.controller.app;


import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.result.Result;
import com.lanf.user.model.dto.*;
import com.lanf.user.model.vo.PublicKeyVO;
import com.lanf.user.model.vo.UserDetailVO;
import com.lanf.user.model.vo.UserTokenInfoVO;
import com.lanf.user.model.vo.UserVO;
import com.lanf.user.service.IUserService;
import com.lanf.web.security.keygen.RsaEncryptKeyManager;
import com.lanf.web.security.keygen.SignKeyManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;


@Slf4j
@RestController
@RequestMapping("/app/user")
public class UserController {

    @Autowired
    private IUserService userService;

    @Autowired
    private RsaEncryptKeyManager publicKeyManager;


    @Autowired
    private SignKeyManager signKeyManager;
    @PostMapping("/register")
    public Result<Void> register(@Validated @RequestBody RegisterUserDTO dto) {

        log.info("[{}]开始,入参:[{}]", "注册", JsonUtils.toJsonString(dto));
        userService.registerUser(dto);
        log.info("[{}]结束", "注册");

        return Result.ok();
    }

    @PostMapping("/registerSendCode")
    public Result<Void> registerSendCode(RegisterSendCodeDTO dto) {

        log.info("[{}]开始,入参:[{}]", "注册发送短信验证码", dto.getPhoneNumber());

        userService.registerSendCode(dto.getPhoneNumber());

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
    public Result<Void> loginSendCode(@Validated @RequestBody LoginSendCodeDTO dto) {

        log.info("[{}]开始,入参:[{}]", "登入发送短信验证码", dto);

        userService.loginSendCode(dto);

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

    @GetMapping("/src/main/test")
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
     * 获取公钥（RSA密钥对）
     * @return 公钥信息
     */
    @PostMapping("/getPublicKey")
    public Result<PublicKeyVO> getPublicKey() {

        log.info("[{}]开始", "获取公钥");

        RsaEncryptKeyManager.PublicKeyInfo publicKeyInfo = publicKeyManager.generatePublicKey();
        
        PublicKeyVO publicKeyVO = new PublicKeyVO();
        publicKeyVO.setRandomKey(publicKeyInfo.getRandomKey());
        publicKeyVO.setPublicKey(publicKeyInfo.getPublicKey());

        log.info("[{}]结束,randomKey:[{}]", "获取公钥", publicKeyInfo.getRandomKey());

        return Result.ok(publicKeyVO);
    }

    /**
     * 获取签名密钥（AES密钥）
     * @return 签名密钥信息
     */
    @PostMapping("/getSignKey")
    public Result<PublicKeyVO> getSignKey() {

        log.info("[{}]开始", "获取签名密钥");

        SignKeyManager.SignKeyInfo signKeyInfo = signKeyManager.generateSignKey();
        
        PublicKeyVO publicKeyVO = new PublicKeyVO();
        publicKeyVO.setRandomKey(signKeyInfo.getSignRandomKey());
        publicKeyVO.setPublicKey(signKeyInfo.getAesKeyBase64());


        return Result.ok(publicKeyVO);
    }
}

