package com.lanf.api.user.api;

import com.lanf.api.user.model.vo.AddressListVO;
import com.lanf.constant.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Component
@FeignClient(name = "service-user",url = "localhost:9006")
public interface UserApiService {

    @GetMapping("/user/api/addressListQuery")
    public Result<List<AddressListVO>> addressListQuery();
}

