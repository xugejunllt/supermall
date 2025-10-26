package com.lanf.welfare.controller.app;


import com.lanf.mybatis.base.PageQuery;
import com.lanf.mybatis.base.PageResult;
import com.lanf.web.result.Result;
import com.lanf.welfare.model.dto.CouponTemplateAddDTO;
import com.lanf.welfare.model.entity.CouponTemplateDO;
import com.lanf.welfare.model.query.CouponTemplatePageQuery;
import com.lanf.welfare.service.biz.ICouponTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping("/app/couponTemplate")
public class CouponTemplateAppController {

    @Autowired
    private ICouponTemplateService couponTemplateService;

    @GetMapping("/couponTemplatePage")
    public Result<PageResult<CouponTemplateDO>> couponTemplatePage(@Validated CouponTemplatePageQuery query) {

        log.info("分页查询优惠券模板:{}:query",query);

        return Result.ok(couponTemplateService.couponTemplatePage(query));
    }

}

