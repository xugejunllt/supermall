package com.lanf.system.controller.company;


import com.lanf.mybatis.base.PageResult;
import com.lanf.system.model.dto.CompanyRegisterDTO;
import com.lanf.system.model.entiry.CompanyDO;
import com.lanf.system.model.query.CompanyPageQuery;
import com.lanf.system.model.vo.CompanyRegisterVO;
import com.lanf.system.service.company.ICompanyService;
import com.lanf.web.result.Result;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-05-28
 */
@Slf4j
@RestController
@RequestMapping("/company")
public class CompanyController {


    @Autowired
    private ICompanyService companyService;

    @ApiOperation(value = "公司注册")
    @PostMapping("/companyRegister")
    public Result<CompanyRegisterVO> companyRegister(@Validated  @RequestBody  CompanyRegisterDTO companyRegisterDTO ) {

        log.info("公司注册:companyRegisterDTO{}",companyRegisterDTO);

        return Result.ok(companyService.companyRegister(companyRegisterDTO));
    }

    @PreAuthorize("hasAuthority('bnt.company.auditing')")
    @ApiOperation(value = "公司信息审核")
    @PostMapping("/auditing")
    public Result auditing(Long id,Integer status ) {

        log.info("公司信息审核:id{},status:{}",id,status);
        companyService.auditing(id,status);
        return Result.ok();
    }

    @PreAuthorize("hasAuthority('bnt.company.list')")
    @ApiOperation(value = "分页查询公司列表")
    @GetMapping("/companyPage")
    public Result<PageResult<CompanyDO>> companyPage(CompanyPageQuery query) {

        log.info("分页查询公司列表:query{}",query);
        return Result.ok(companyService.companyPage(query));
    }

}

