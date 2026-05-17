package com.lanf.system.controller.app;


import com.lanf.common.utils.JsonUtils;
import com.lanf.system.model.dto.MerchantRegisterDTO;
import com.lanf.system.service.merchant.IMerchantService;
import com.lanf.constant.result.Result;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

        log.info("[{}]开始,入参:[{}]", "商家注册", JsonUtils.toJsonString(companyRegister));

        merchantService.registerMerchant(companyRegister);

        log.info("[{}]结束", "商家注册");
        return Result.ok();
    }



}

