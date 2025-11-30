package com.lanf.user.controller.app;


import com.lanf.common.utils.JsonUtils;
import com.lanf.security.utils.UserIdContext;
import com.lanf.user.model.dto.CreateAddressDTO;
import com.lanf.user.model.dto.SetDefaultAddressDTO;
import com.lanf.user.model.vo.AddressVO;
import com.lanf.user.service.IAddressService;
import com.lanf.constant.result.Result;
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

        Long userId = UserIdContext.getUserId();
        log.info("用户[{}][{}]开始,入参:[{}]",userId, "添加收货地址", JsonUtils.toJsonString(dto));
        dto.setUserId(userId);
        addressService.createAddress(dto);

        log.info("[{}]结束", "添加收货地址");

        return Result.ok();
    }


    @GetMapping("/listAddress")
    public Result<List<AddressVO>> listAddress() {

        log.info("用户[{}][{}]开始", UserIdContext.getUserId(),"获取地址列表");

        return Result.ok(addressService.listAddress());
    }

    @PostMapping("/setDefaultAddress")
    public Result setDefaultAddress(@Validated @RequestBody SetDefaultAddressDTO dto) {

        Long userId = UserIdContext.getUserId();
        log.info("用户[{}][{}]开始,入参:[{}]",userId, "设置地址为默认地址", JsonUtils.toJsonString(dto));
        dto.setUserId(userId);
        log.info("[{}]结束", "设置地址为默认地址");
        addressService.setDefaultAddress(dto);
        return Result.ok();
    }

}

