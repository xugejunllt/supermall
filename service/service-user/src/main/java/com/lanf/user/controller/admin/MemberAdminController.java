package com.lanf.user.controller.admin;


import com.lanf.mybatis.base.PageResult;
import com.lanf.user.model.dto.LoginOutDTO;
import com.lanf.user.model.dto.UserLoginDTO;
import com.lanf.user.model.dto.UserRegisterDTO;
import com.lanf.user.model.query.UserPageQuery;
import com.lanf.user.model.vo.UserLoginVO;
import com.lanf.user.model.vo.UserPageVO;
import com.lanf.user.service.IMemberService;
import com.lanf.web.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping("/admin/user")
public class MemberAdminController {

    @Autowired
    private IMemberService memberService;

    @GetMapping("/userPage")
    public Result<PageResult<UserPageVO>> userPage(UserPageQuery query) {

        log.info("分页查询用户列表:{}", query);

        return Result.ok(memberService.userPage(query));
    }


}

