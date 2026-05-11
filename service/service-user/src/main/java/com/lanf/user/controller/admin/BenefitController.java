package com.lanf.user.controller.admin;


import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import com.lanf.user.model.dto.AddBenefitDTO;
import com.lanf.user.model.dto.CalculationGrowthValueDTO;
import com.lanf.user.model.dto.DisableBenefitDTO;
import com.lanf.user.model.dto.UseBenefitDTO;
import com.lanf.user.model.entity.BenefitDO;
import com.lanf.user.model.query.BenefitPageQuery;
import com.lanf.user.service.benefit.IBenefitService;
import com.lanf.user.service.benefit.IUserLevelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * <p>
 * 权益表
 * 前端控制器
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
    @Autowired
    private IUserLevelService userLevelService;

    @PostMapping("/addBenefit")
    public Result<Void> addBenefit(@Validated @RequestBody AddBenefitDTO dto) {

        log.info("添加权益开始,参数:{}", dto);

        benefitService.addBenefit(dto);

        return Result.ok();
    }


    @PostMapping("/useBenefit")
    public Result<Void> useBenefit(@RequestBody UseBenefitDTO dto) {

        log.info("使用权益开始,参数:{}", dto);

        benefitService.useBenefit(dto);

        return Result.ok();
    }

    @PostMapping("/disableBenefit")
    public Result<Void> disableBenefit(@RequestBody DisableBenefitDTO dto) {

        log.info("禁用权益开始,参数:{}", dto);

        benefitService.disableBenefit(dto);

        return Result.ok();
    }

    @PostMapping("/benefitPageQuery")
    public Result<PageResult<BenefitDO>> benefitPageQuery(@Validated @RequestBody BenefitPageQuery query) {

        log.info("分页查询权益开始,参数:{}", query);

        return Result.ok(benefitService.benefitPageQuery(query));
    }


    @PostMapping("/calculationGrowthValue")
    public Result<Void> calculationGrowthValue(@Validated @RequestBody CalculationGrowthValueDTO dto) {

        log.info("计算成长值开始,参数:{}", dto);

        userLevelService.calculationGrowthValue(dto);

        return Result.ok();
    }

}

