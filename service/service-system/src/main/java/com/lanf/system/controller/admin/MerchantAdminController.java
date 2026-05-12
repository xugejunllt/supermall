package com.lanf.system.controller.admin;


import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import com.lanf.system.model.entiry.MerchantDO;
import com.lanf.system.model.query.CompanyPageQuery;
import com.lanf.system.service.merchant.IMerchantService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
@RequestMapping("/admin/merchant")
public class MerchantAdminController {


    @Autowired
    private IMerchantService merchantService;

    //@PreAuthorize("hasAuthority('bnt.company.auditing')")
    @ApiOperation(value = "审核通过")
    @PostMapping("/auditApprove")
    public Result auditApprove(Long id) {

        log.info("[{}]开始,入参:[{}]", "审核商家", id);

        merchantService.auditApprove(id);

        log.info("[{}]结束", "审核商家");

        return Result.ok();
    }

    @PreAuthorize("hasAuthority('bnt.company.list')")
    @ApiOperation(value = "分页查询公司列表")
    @GetMapping("/companyPage")
    public Result<PageResult<MerchantDO>> companyPage(CompanyPageQuery query) {

        log.info("[{}]开始,入参:[{}]", "分页查询公司商家列表", JsonUtils.toJsonString(query));

        return Result.ok(merchantService.merchantPage(query));
    }

}

