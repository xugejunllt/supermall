package com.lanf.api.user.api;

import com.lanf.api.user.model.vo.AddressListVO;
import com.lanf.api.user.model.vo.UserPageVO;
import com.lanf.constant.model.query.PageQuery;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class UserFeignClientFallback implements UserApiService{
    @Override
    public Result<List<AddressListVO>> addressListQuery() {
        return Result.fail("user服务降级");
    }

    @Override
    public Result<List<AddressListVO>> addressListByUserIdQuery(Long userId) {
        return Result.fail("user服务降级");
    }

    @Override
    public Result<PageResult<UserPageVO>> userPageQuery(PageQuery query) {
        return Result.fail("user服务降级");
    }

    @Override
    public Result<String> sentinelTest() {

        return Result.fail("user服务降级");
    }
}
