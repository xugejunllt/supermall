package com.lanf.welfare.controller.admin;


import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import com.lanf.constant.utils.UserContext;
import com.lanf.welfare.model.dto.AddCouponTemplateDTO;
import com.lanf.welfare.model.dto.RevokeCouponTemplateDTO;
import com.lanf.welfare.model.query.CouponTemplateForAdminPageQuery;
import com.lanf.welfare.model.vo.CouponPurposeVO;
import com.lanf.welfare.model.vo.CouponTemplatePageVO;
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
    public Result<Void> couponTemplateAdd(@Validated @RequestBody AddCouponTemplateDTO dto) {

        log.info("添加优惠券模板:{}:dto", dto);
        dto.setAdminUserId(UserContext.getUserId());
        couponTemplateService.addCouponTemplate(dto);

        return Result.ok();
    }

    /**
     * 获取优惠券用途列表
     */
    @GetMapping("/couponPurposeListQuery")
    public Result<List<CouponPurposeVO>> couponPurposeListQuery() {

        log.info("查询优惠卷用途");

        return Result.ok(couponTemplateService.couponPurposeListQuery());
    }

    @GetMapping("/couponTemplatePageQuery")
    public Result<PageResult<CouponTemplatePageVO>> couponTemplatePageQuery(@Validated CouponTemplateForAdminPageQuery query) {

        log.info("分页查询优惠券模板:{}", query);

        return Result.ok(couponTemplateService.couponTemplatePageQuery(query));
    }

    /**
     * 撤销优惠券模板
     */
    @PostMapping("/revokeCouponTemplate")
    public Result<Void> revokeCouponTemplate(@RequestBody @Validated RevokeCouponTemplateDTO dto) {

        log.info("撤销优惠券模板[{}]", dto.getCouponTemplateId());

        couponTemplateService.revokeCouponTemplate(dto);

        return Result.ok();

    }

}

