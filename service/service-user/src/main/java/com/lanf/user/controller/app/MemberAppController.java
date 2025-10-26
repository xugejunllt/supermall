package com.lanf.user.controller.app;


import com.lanf.user.model.dto.LoginOutDTO;
import com.lanf.user.model.dto.UserLoginDTO;
import com.lanf.user.model.dto.UserRegisterDTO;
import com.lanf.user.model.vo.UserLoginVO;
import com.lanf.user.service.IMemberService;
import com.lanf.web.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-10
 */
@Slf4j
@RestController
@RequestMapping("/app/member")
public class MemberAppController {

    @Autowired
    private IMemberService memberService;

    @PostMapping("/register")
    public Result register(@Validated @RequestBody UserRegisterDTO dto) {
        log.info("用户注册:dto{}", dto);
        memberService.userRegister(dto);
        return Result.ok();
    }

    @PostMapping("/login")
    public Result<UserLoginVO> login(@Validated @RequestBody UserLoginDTO dto) {

        log.info("用户登入:dto{}", dto);

        return Result.ok(memberService.userLogin(dto));
    }

    @PostMapping("/loginOut")
    public Result loginOut(@Validated @RequestBody LoginOutDTO dto) {

        log.info("退出登入:dto{}", dto);
        memberService.loginOut(dto);
        return Result.ok();
    }

    @PostMapping("/sendLoginCode")
    public Result sendCode() {

        log.info("发送短信验证码");

        return Result.ok();
    }

}

