package com.lanf.user.controller.admin;

import com.lanf.api.user.model.vo.AddressListVO;
import com.lanf.api.user.model.vo.UserPageVO;
import com.lanf.constant.model.query.PageQuery;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import com.lanf.user.service.IAddressService;
import com.lanf.user.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin")
public class UserAdminController {

    @Autowired
    private IAddressService addressService;

    @Autowired
    private IUserService userService;


    /**
     * 根据用户id查询地址列表
     */
    @GetMapping("/addressListByUserIdQuery")
    public Result<List<AddressListVO>> addressListByUserIdQuery(@RequestParam("userId") Long userId) {
        log.info("[{}]开始,userId:[{}]", "根据用户id查询地址列表", userId);
        return Result.ok(addressService.addressListByUserIdQuery(userId));
    }

    /**
     * 分页查询用户列表
     */
    @GetMapping("/userPageQuery")
    public Result<PageResult<UserPageVO>> userPageQuery(PageQuery query) {
        log.info("[{}]开始,入参:[{}]", "分页查询用户列表", query);
        return Result.ok(userService.userPageQuery(query));
    }

}
