package com.lanf.user.controller.app;


import com.lanf.constant.result.Result;
import com.lanf.user.model.dto.CreateAddressDTO;
import com.lanf.user.model.dto.SetDefaultAddressDTO;
import com.lanf.user.model.vo.AddressVO;
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

    @PostMapping("/createAddress")
    public Result<Void>  createAddress(@Validated @RequestBody CreateAddressDTO dto){

        Long userId = UserContext.getUserId();
        log.info("添加收货地址开始,用户ID:{},参数:{}", userId, dto);
        dto.setUserId(userId);
        addressService.createAddress(dto);

        return Result.ok();
    }


    @GetMapping("/listAddress")
    public Result<List<AddressVO>> listAddress() {

        Long userId = UserContext.getUserId();
        log.info("获取地址列表开始,用户ID:{}", userId);

        return Result.ok(addressService.listAddress());
    }

    @PostMapping("/setDefaultAddress")
    public Result setDefaultAddress(@Validated @RequestBody SetDefaultAddressDTO dto) {

        Long userId = UserContext.getUserId();
        log.info("设置默认地址开始,用户ID:{},参数:{}", userId, dto);
        dto.setUserId(userId);
        addressService.setDefaultAddress(dto);
        return Result.ok();
    }

}

