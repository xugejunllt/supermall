package com.lanf.user.controller.app;


import com.lanf.user.model.dto.RegisterUserDTO;
import com.lanf.user.service.IUserService;
import com.lanf.web.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;


@Slf4j
@RestController
@RequestMapping("/app/user")
public class UserController {

    @Autowired
    private IUserService userService;


    @PostMapping("/register")
    public Result<Void> register(@RequestBody  RegisterUserDTO dto) {

        log.info("[{}]开始,入参:[{}]","注册",dto);
        userService.registerUser(dto);
        log.info("[{}]结束","注册");

        return Result.ok();
    }

    @PostMapping("/registerSendCode")
    public Result<Void> registerSendCode(@NotNull( message = "手机号不能为空") String phoneNumber) {

        log.info("[{}]开始,入参:[{}]","注册发送短信验证码",phoneNumber);

        userService.registerSendCode(phoneNumber);

        log.info("[{}]结束","注册发送短信验证码");

        return Result.ok();
    }





























}

