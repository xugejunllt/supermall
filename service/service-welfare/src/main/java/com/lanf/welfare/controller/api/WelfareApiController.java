package com.lanf.welfare.controller.api;


import com.lanf.constant.result.Result;
import com.lanf.welfare.model.dto.CalculateDiscountAmountDTO;
import com.lanf.welfare.model.dto.UseMultipleCouponDTO;
import com.lanf.welfare.model.vo.CalculateDiscountAmountVO;
import com.lanf.welfare.service.ICouponService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
public class WelfareApiController {

    @Autowired
    private ICouponService couponService;

    /**
     * 计算可使用优惠卷 优惠信息
     */
    @PostMapping("/calculateDiscountAmount")
    public Result<CalculateDiscountAmountVO> calculateDiscountAmount(@RequestBody @Validated CalculateDiscountAmountDTO dto) {

        log.info("计算可使用优惠卷优惠信息:{}",dto);
        return Result.ok( couponService.calculateDiscountAmount(dto));
    }
    /**
     * 使用多张优惠卷
     */
    @PostMapping("/useMultipleCoupon")
    public Result<CalculateDiscountAmountVO> useMultipleCoupon(@RequestBody @Validated UseMultipleCouponDTO dto) {

        log.info("使用多张优惠卷:{}",dto);
        return Result.ok( couponService.useMultipleCoupon(dto));
    }


}
