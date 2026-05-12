package com.lanf.welfare.controller.app;


import com.lanf.constant.result.Result;
import com.lanf.welfare.model.dto.ReceiveShopCouponDTO;
import com.lanf.welfare.service.ICouponService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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



    /**
     * 领取店铺优惠券
     */
    @PostMapping("/receiveShopCoupon")
    public Result<Void> receiveShopCoupon(@RequestBody @Validated ReceiveShopCouponDTO dto) {
        dto.setUserId(UserIdContext.getUserId());
        log.info("领取店铺优惠券:{}",dto);

        couponService.receiveShopCoupon(dto);
        return Result.ok();
    }

}

