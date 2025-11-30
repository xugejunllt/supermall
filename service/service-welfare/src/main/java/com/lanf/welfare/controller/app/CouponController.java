package com.lanf.welfare.controller.app;


import com.lanf.constant.result.Result;
import com.lanf.welfare.model.dto.ReceiveCouponDTO;
import com.lanf.welfare.model.vo.ShopCouponVO;
import com.lanf.welfare.service.biz.ICouponService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 优惠券 前端控制器
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-08-01
 */
@Slf4j
@RestController
@RequestMapping("/app/coupon")
public class CouponController {

    @Autowired
    private ICouponService couponService;

    @PostMapping("/receiveCoupon")
    public Result receiveCoupon(@Validated @RequestBody ReceiveCouponDTO dto) {

        log.info("领取优惠券:{}:dto:",dto);
        couponService.receiveCoupon(dto);
        return Result.ok();
    }

    @GetMapping("/shopCouponList")
    public Result<List<ShopCouponVO>> shopCouponList(Long shopId) {

        log.info("查询店铺可使用优惠券列表:{}:shopId:",shopId);

        return Result.ok(couponService.shopCouponList(shopId));
    }


}

