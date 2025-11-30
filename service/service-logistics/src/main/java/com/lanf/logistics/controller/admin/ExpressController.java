package com.lanf.logistics.controller.admin;


import com.lanf.logistics.model.dto.ExpressAddDTO;
import com.lanf.logistics.model.entity.ExpressDO;
import com.lanf.logistics.service.IExpressService;
import com.lanf.mybatis.base.PageQuery;
import com.lanf.mybatis.base.PageResult;
import com.lanf.constant.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-17
 */
@Slf4j
@RestController
@RequestMapping("/express")
public class ExpressController {

    @Autowired
    private IExpressService expressService;

    @PreAuthorize("hasAuthority('bnt.company.list')")
    @PostMapping("/expressAdd")
    public Result expressAdd(@Validated @RequestBody ExpressAddDTO dto) {

        log.info("添加快递公司");
        expressService.expressAdd(dto);
        return Result.ok();
    }

    @GetMapping("/expressPage")
    public Result<PageResult<ExpressDO>> expressPage(@Validated PageQuery query) {

        log.info("分页查询快递公司:query{}", query);
        return Result.ok(expressService.expressPage(query));
    }

}

