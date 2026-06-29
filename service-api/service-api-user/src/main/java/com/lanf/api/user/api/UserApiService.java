package com.lanf.api.user.api;

import com.lanf.api.user.model.vo.AddressListVO;
import com.lanf.api.user.model.vo.UserPageVO;
import com.lanf.constant.model.query.PageQuery;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Component
@FeignClient(name = "service-user",fallback = UserFeignClientFallback.class)
public interface UserApiService {

    @GetMapping("/user/api/addressListQuery")
    public Result<List<AddressListVO>> addressListQuery();

    /**
     * 根据用户id查询地址列表
     */
    @GetMapping("/user/admin/addressListByUserIdQuery")
    public Result<List<AddressListVO>> addressListByUserIdQuery(@RequestParam("userId") Long userId);

    /**
     * 分页查询用户列表
     */
    @GetMapping("/user/admin/userPageQuery")
    public Result<PageResult<UserPageVO>> userPageQuery(@SpringQueryMap PageQuery query);

    /**
     * 测试sentinel 熔断
     *
     */
    @GetMapping("/user/api/sentinelTest")
    public Result<String> sentinelTest();
}

