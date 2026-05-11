package com.lanf.user.controller.app;


import com.lanf.constant.result.Result;
import com.lanf.user.model.dto.AddAddressDTO;
import com.lanf.user.model.dto.SetDefaultAddressDTO;
import com.lanf.user.model.vo.AddressListVO;
import com.lanf.user.service.IAddressService;
import com.lanf.web.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author
 * @since 2024-06-10
 */
@Slf4j
@RestController
@RequestMapping("/app/address")
public class AddressAppController {

    @Autowired
    private IAddressService addressService;

    @PostMapping("/addAddress")
    public Result<Void> addAddress(@Validated @RequestBody AddAddressDTO dto) {

        Long userId = UserContext.getUserId();
        log.info("添加收货地址开始,用户ID:{},参数:{}", userId, dto);
        dto.setUserId(userId);
        addressService.addAddress(dto);

        return Result.ok();
    }


    @PostMapping("/addressListQuery")
    public Result<List<AddressListVO>> addressListQuery() {

        Long userId = UserContext.getUserId();
        log.info("获取地址列表开始,用户ID:{}", userId);

        return Result.ok(addressService.addressListQuery());
    }

    @PostMapping("/setDefaultAddress")
    public Result<Void> setDefaultAddress(@Validated @RequestBody SetDefaultAddressDTO dto) {

        Long userId = UserContext.getUserId();
        log.info("设置默认地址开始,用户ID:{},参数:{}", userId, dto);
        dto.setUserId(userId);
        addressService.setDefaultAddress(dto);
        return Result.ok();
    }

}

