package com.lanf.system.controller.app;


import com.lanf.common.utils.JsonUtils;
import com.lanf.mybatis.base.PageResult;
import com.lanf.system.model.dto.MerchantRegisterDTO;
import com.lanf.system.model.entiry.MerchantDO;
import com.lanf.system.model.query.CompanyPageQuery;
import com.lanf.system.service.merchant.IMerchantService;
import com.lanf.web.result.Result;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @ApiOperation(value = "公司注册")
    @PostMapping("/registerMerchant")
    public Result<Void> registerMerchant(@RequestBody MerchantRegisterDTO companyRegister) {

        log.info("[{}]开始,入参:[{}]", "商家注册", JsonUtils.toJsonString(companyRegister));

        merchantService.registerMerchant(companyRegister);

        log.info("[{}]结束", "商家注册");
        return Result.ok();
    }



}

