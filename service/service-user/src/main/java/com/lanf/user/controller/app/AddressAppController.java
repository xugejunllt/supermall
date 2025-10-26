package com.lanf.user.controller.app;


import com.lanf.user.model.dto.AddressAddDTO;
import com.lanf.user.model.dto.SetDefaultAddressDTO;
import com.lanf.user.model.entity.AddressDO;
import com.lanf.user.model.vo.UserLoginVO;
import com.lanf.user.service.IAddressService;
import com.lanf.web.result.Result;
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
 * @author 江帅帅 Jss_forever
 * @since 2024-06-10
 */
@Slf4j
@RestController
@RequestMapping("/app/address")
public class AddressAppController {

    @Autowired
    private IAddressService addressService;

    @PostMapping("/addAddress")
    public Result<UserLoginVO> addAddress(@Validated @RequestBody AddressAddDTO dto) {

        log.info("添加用户地址:dto{}", dto);

        addressService.addAddress(dto);
        return Result.ok();
    }

    @GetMapping("/getDefaultAddress")
    public Result<AddressDO> getDefaultAddress() {
        log.info("获取默认地址");
        return Result.ok(addressService.getDefaultAddress());
    }

    @GetMapping("/addressList")
    public Result<List<AddressDO>> addressList() {
        log.info("获取地址列表");
        return Result.ok(addressService.addressList());
    }

    @PostMapping("/setDefaultAddress")
    public Result setDefaultAddress(@Validated @RequestBody SetDefaultAddressDTO dto) {

        log.info("设置地址为默认地址dto:{}", dto);
        addressService.setDefaultAddress(dto);
        return Result.ok();
    }

}

