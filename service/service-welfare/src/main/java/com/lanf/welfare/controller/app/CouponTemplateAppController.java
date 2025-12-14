package com.lanf.welfare.controller.app;


import com.lanf.constant.result.Result;
import com.lanf.welfare.model.vo.CouponTemplateListVO;
import com.lanf.welfare.service.ICouponTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
@RequestMapping("/app/couponTemplate")
public class CouponTemplateAppController {

    @Autowired
    private ICouponTemplateService couponTemplateService;

    @GetMapping("/listCouponTemplate")
    public Result< List<CouponTemplateListVO>> listCouponTemplateList(@Validated Long shopId) {

        log.info("查询优惠券模板列表:{}:query",shopId);

        return Result.ok(couponTemplateService.listCouponTemplate(shopId));
    }

}

