package com.lanf.welfare.api;


import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import com.lanf.welfare.model.dto.*;
import com.lanf.welfare.model.query.CouponTemplateForAdminPageQuery;
import com.lanf.welfare.model.vo.*;
import org.dromara.hmily.annotation.Hmily;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Set;

@Component
@FeignClient(name = "service-welfare") //调用的服务名称
public interface WelfareApiService {

    /**
     * 优惠券计算
     *
     *
     */
    @PostMapping("/welfare/api/calculateDiscountAmount")
    public Result<CalculateDiscountAmountVO> calculateDiscountAmount(@RequestBody
                                                                         CalculateDiscountAmountDTO dto);
    /**
     * 使用多张优惠卷
     *
     *
     */
    @Hmily
    @PostMapping("/welfare/api/useMultipleCoupon")
    public Result<CalculateDiscountAmountVO> useMultipleCoupon(@RequestBody
                                                               UseMultipleCouponDTO dto);


    @PostMapping("/welfare/api/bathUseCoupon")
    public Result<List<UseCouponVO>> bathUseCoupon(@Validated @RequestBody List<UseCouponDTO> dtoList);

    @PostMapping("/welfare/api/queryByIdSet")
    public Result<List<CouponVO>> queryByIdSet(@Validated @RequestBody Set<Long> idSet);

    /**
     * 添加优惠券模板
     */
    @PostMapping("/welfare/admin/couponTemplate/addCouponTemplate")
    public Result<Void> addCouponTemplate(@Validated @RequestBody AddCouponTemplateDTO dto);

    /**
     * 获取优惠券用途列表
     */
    @GetMapping("/welfare/admin/couponTemplate/couponPurposeListQuery")
    public Result<List<CouponPurposeVO>> couponPurposeListQuery();

    /**
     * 分页查询优惠券模板
     */
    @GetMapping("/welfare/admin/couponTemplate/couponTemplatePageQuery")
    public Result<PageResult<CouponTemplatePageVO>> couponTemplatePageQuery(@SpringQueryMap CouponTemplateForAdminPageQuery query);

    /**
     * 撤销优惠券模板
     */
    @PostMapping("/welfare/admin/couponTemplate/revokeCouponTemplate")
    public Result<Void> revokeCouponTemplate(@RequestBody @Validated RevokeCouponTemplateDTO dto);
}
