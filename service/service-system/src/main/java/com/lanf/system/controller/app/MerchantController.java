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
    private IMerchantService companyService;

    @ApiOperation(value = "公司注册")
    @PostMapping("/registerMerchant")
    public Result<Void> registerMerchant(@RequestBody MerchantRegisterDTO companyRegister) {

        log.info("[{}]开始,入参:[{}]", "商家注册", JsonUtils.toJsonString(companyRegister));

        companyService.registerMerchant(companyRegister);

        log.info("[{}]结束", "商家注册");
        return Result.ok();
    }

    //@PreAuthorize("hasAuthority('bnt.company.auditing')")
    @ApiOperation(value = "审核通过")
    @PostMapping("/auditApprove")
    public Result auditApprove(Long id) {

        log.info("[{}]开始,入参:[{}]", "审核商家", id);

        companyService.auditApprove(id);

        log.info("[{}]结束", "审核商家");

        return Result.ok();
    }

    @PreAuthorize("hasAuthority('bnt.company.list')")
    @ApiOperation(value = "分页查询公司列表")
    @GetMapping("/companyPage")
    public Result<PageResult<MerchantDO>> companyPage(CompanyPageQuery query) {

        log.info("分页查询公司列表:query{}", query);
        return Result.ok(companyService.companyPage(query));
    }

}

