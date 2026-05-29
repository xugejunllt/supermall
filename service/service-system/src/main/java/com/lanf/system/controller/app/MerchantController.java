package com.lanf.system.controller.app;


import com.lanf.constant.result.Result;
import com.lanf.system.model.dto.MerchantRegisterDTO;
import com.lanf.system.service.merchant.IMerchantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-05-28
 */
@Slf4j
@RestController
@RequestMapping("/app/merchant")
public class MerchantController {


    @Autowired
    private IMerchantService merchantService;

    @PostMapping("/registerMerchant")
    public Result<Void> registerMerchant(@RequestBody MerchantRegisterDTO companyRegister) {

        log.info("[{}]开始,入参:[{}]", "商家注册",companyRegister);

        merchantService.registerMerchant(companyRegister);

        return Result.ok();
    }



}

