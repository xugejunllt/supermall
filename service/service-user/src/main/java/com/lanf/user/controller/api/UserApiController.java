package com.lanf.user.controller.api;

import com.lanf.api.user.model.vo.AddressListVO;
import com.lanf.constant.result.Result;
import com.lanf.user.service.IAddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
public class UserApiController {

    @Autowired
    private IAddressService addressService;

    @GetMapping("/addressListQuery")
    public Result<List<AddressListVO>> addressListQuery() {

        return Result.ok(addressService.addressListQuery());

    }

    @GetMapping("/sentinelTest")
    public Result<String> sentinelTest() {


        int i = 1 / 0;
        return Result.ok();

    }
}
