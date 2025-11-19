package com.lanf.user.controller.admin;


import com.lanf.common.utils.JsonUtils;
import com.lanf.mybatis.base.PageResult;
import com.lanf.user.model.dto.CreateBenefitDTO;
import com.lanf.user.model.dto.RegisterUserDTO;
import com.lanf.user.model.entity.BenefitDO;
import com.lanf.user.model.query.BenefitPageQuery;
import com.lanf.user.service.benefit.IBenefitService;
import com.lanf.web.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

/**
 * <p>
 * 权益表
 前端控制器
 * </p>
 *
 * @author jarven
 * @since 2025-11-19
 */
@Slf4j
@RestController
@RequestMapping("/admin/benefit")
public class BenefitController {

    @Autowired
    private IBenefitService benefitService;

    @PostMapping("/createBenefit")
    public Result<Void> createBenefit(@Validated @RequestBody CreateBenefitDTO dto) {

        log.info("[{}]开始,入参:[{}]", "添加权益", JsonUtils.toJsonString(dto));

        benefitService.createBenefit(dto);

        log.info("[{}]结束", "添加权益");

        return Result.ok();
    }


    @PostMapping("/useBenefit")
    public Result<Void> useBenefit(@NotNull(message = "id不能为空")  Long id) {

        log.info("[{}]开始,入参:[{}]", "使用权益", id);

        benefitService.useBenefit(id);

        log.info("[{}]结束", "使用权益");

        return Result.ok();
    }

    @PostMapping("/disableBenefit")
    public Result<Void> disableBenefit(@NotNull(message = "id不能为空")  Long id) {

        log.info("[{}]开始,入参:[{}]", "禁用权益", id);

        benefitService.disableBenefit(id);

        log.info("[{}]结束", "禁用权益");

        return Result.ok();
    }

    @GetMapping("/pageBenefit")
    public Result<PageResult<BenefitDO>> pageBenefit(@Validated BenefitPageQuery query) {

        log.info("[{}]开始,入参:[{}]", "分页查询权益", query);

        return Result.ok(benefitService.pageBenefit(query));
    }





}

