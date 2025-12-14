package com.lanf.welfare.controller.admin;


import com.lanf.constant.result.Result;
import com.lanf.mybatis.base.PageResult;
import com.lanf.security.utils.UserIdContext;
import com.lanf.welfare.model.dto.CouponTemplateAddDTO;
import com.lanf.welfare.model.entity.CouponTemplateDO;
import com.lanf.welfare.model.query.CouponTemplatePageQuery2;
import com.lanf.welfare.model.vo.CouponPurposeVO;
import com.lanf.welfare.service.ICouponTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 优惠券模板 前端控制器
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-08-01
 */
@Slf4j
@RestController
@RequestMapping("/admin/couponTemplate")
public class CouponTemplateAdminController {

    @Autowired
    private ICouponTemplateService couponTemplateService;

    @PostMapping("/couponTemplateAdd")
    public Result couponTemplateAdd(@Validated @RequestBody CouponTemplateAddDTO dto) {

        log.info("添加优惠券模板:{}:dto", dto);
        dto.setAdminUserId(UserIdContext.getUserId());
        couponTemplateService.couponTemplateAdd(dto);

        return Result.ok();
    }
    /**
     * 获取优惠券用途列表
     */
    @GetMapping("/purpose/list")
    public Result<List<CouponPurposeVO>> getCouponPurposeList() {

           log.info("查询优惠卷用途");

        return Result.ok(couponTemplateService.couponPurposeList());
    }

    @GetMapping("/couponTemplatePage")
    public Result<PageResult<CouponTemplateDO>> couponTemplatePage(@Validated CouponTemplatePageQuery2 query) {

        log.info("分页查询优惠券模板:{}", query);

        return Result.ok(couponTemplateService.couponTemplatePage(query));
    }

}

