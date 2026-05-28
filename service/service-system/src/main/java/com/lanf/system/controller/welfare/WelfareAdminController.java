package com.lanf.system.controller.welfare;

import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import com.lanf.welfare.api.WelfareApiService;
import com.lanf.welfare.model.dto.AddCouponTemplateDTO;
import com.lanf.welfare.model.dto.RevokeCouponTemplateDTO;
import com.lanf.welfare.model.query.CouponTemplateForAdminPageQuery;
import com.lanf.welfare.model.vo.CouponPurposeVO;
import com.lanf.welfare.model.vo.CouponTemplatePageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/welfare")
public class WelfareAdminController {

    @Autowired
    private WelfareApiService welfareApiService;

    @PostMapping("/addCouponTemplate")
    public Result<Void> addCouponTemplate(@Validated @RequestBody AddCouponTemplateDTO dto) {
        log.info("添加优惠券模板:{}", dto);
        return welfareApiService.addCouponTemplate(dto);
    }

    @GetMapping("/couponPurposeListQuery")
    public Result<List<CouponPurposeVO>> couponPurposeListQuery() {
        log.info("查询优惠券用途列表");
        return welfareApiService.couponPurposeListQuery();
    }

    @GetMapping("/couponTemplatePageQuery")
    public Result<PageResult<CouponTemplatePageVO>> couponTemplatePageQuery(@Validated CouponTemplateForAdminPageQuery query) {
        log.info("分页查询优惠券模板:{}", query);
        return welfareApiService.couponTemplatePageQuery(query);
    }

    @PostMapping("/revokeCouponTemplate")
    public Result<Void> revokeCouponTemplate(@RequestBody @Validated RevokeCouponTemplateDTO dto) {
        log.info("撤销优惠券模板:{}", dto);
        return welfareApiService.revokeCouponTemplate(dto);
    }
}
