package com.lanf.user.controller.app;


import com.lanf.constant.result.Result;
import com.lanf.user.model.dto.*;
import com.lanf.user.model.vo.PublicKeyVO;
import com.lanf.user.model.vo.UserDetailVO;
import com.lanf.user.model.vo.UserTokenInfoVO;
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

    @PostMapping("/registerSendCode")
    public Result<Void> registerSendCode(@Validated @RequestBody RegisterSendCodeDTO dto) {

        log.info("注册发送短信验证码开始,参数:{}", dto.getPhoneNumber());

        userService.registerSendCode(dto.getPhoneNumber());

        return Result.ok();
    }
    @PostMapping("/register")
    public Result<Void> register(@Validated @RequestBody RegisterUserDTO dto) {
          
        log.info("注册开始,参数:{}", dto);
        userService.registerUser(dto);

        return Result.ok();
    }

    @PostMapping("/loginSendCode")
    public Result<Void> loginSendCode(@Validated @RequestBody LoginSendCodeDTO dto) {

        log.info("登入发送短信验证码开始,参数:{}", dto);

        userService.loginSendCode(dto);

        return Result.ok();
    }

    @PostMapping("/login")
    public Result<UserTokenInfoVO> login(@Validated @RequestBody LoginUserDTO dt, HttpServletRequest request) {

        log.info("登入开始,参数:{}", dt);

        UserTokenInfoVO userVO = userService.login(dt,request);

        return Result.ok(userVO);
    }




    @PostMapping("/refreshToken")
    public Result<UserTokenInfoVO> refreshToken(@Validated @RequestBody RefreshTokenDTO dto,HttpServletRequest request) {

        log.info("刷新token开始,参数:{}", dto);

        UserTokenInfoVO refreshTokenVO = userService.refreshToken(dto,request);

        return Result.ok(refreshTokenVO);
    }

    @GetMapping("/userDetailQuery")
    public Result<UserDetailVO> userDetailQuery() {

        log.info("获取用户详细开始");

        return Result.ok(userService.userDetailQuery());
    }

    /**
     * 获取公钥（RSA密钥对）
     * @return 公钥信息
     */
    @PostMapping("/getPublicKey")
    public Result<PublicKeyVO> getPublicKey() {

        log.info("获取公钥开始");

        RsaEncryptKeyManager.PublicKeyInfo publicKeyInfo = publicKeyManager.generatePublicKey();
        
        PublicKeyVO publicKeyVO = new PublicKeyVO();
        publicKeyVO.setRandomKey(publicKeyInfo.getRandomKey());
        publicKeyVO.setPublicKey(publicKeyInfo.getPublicKey());

        return Result.ok(publicKeyVO);
    }
    /**
     * 获取签名密钥（AES密钥）
     * @return 签名密钥信息
     */
    @PostMapping("/getSignKey")
    public Result<PublicKeyVO> getSignKey() {

        log.info("获取签名密钥开始");

        SignKeyManager.SignKeyInfo signKeyInfo = signKeyManager.generateSignKey();

        PublicKeyVO publicKeyVO = new PublicKeyVO();
        publicKeyVO.setRandomKey(signKeyInfo.getSignRandomKey());
        publicKeyVO.setPublicKey(signKeyInfo.getAesKeyBase64());


        return Result.ok(publicKeyVO);
    }


}

