package com.lanf.system.controller.user;

import com.lanf.api.user.api.UserApiService;
import com.lanf.api.user.model.vo.AddressListVO;
import com.lanf.api.user.model.vo.UserPageVO;
import com.lanf.constant.model.query.PageQuery;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户管理控制器（远程调用）
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserAdminController {

    @Autowired
    private UserApiService userApiService;

    /**
     * 分页查询用户列表
     */
    @GetMapping("/userPageQuery")
    public Result<PageResult<UserPageVO>> userPageQuery(PageQuery query) {
        log.info("[{}]开始,入参:[{}]", "分页查询用户列表", query);
        return userApiService.userPageQuery(query);
    }

    /**
     * 根据用户id查询地址列表
     */
    @GetMapping("/addressListByUserIdQuery")
    public Result<List<AddressListVO>> addressListByUserIdQuery(@RequestParam("userId") Long userId) {
        log.info("[{}]开始,userId:[{}]", "根据用户id查询地址列表", userId);
        return userApiService.addressListByUserIdQuery(userId);
    }

}
