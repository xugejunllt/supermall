package com.lanf.welfare.controller.app;


import com.lanf.constant.model.query.PageQuery;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import com.lanf.constant.utils.UserContext;
import com.lanf.welfare.model.dto.ReceiveShopCouponDTO;
import com.lanf.welfare.model.vo.CouponPageVO;
import com.lanf.welfare.service.ICouponService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
        dto.setUserId(UserContext.getUserId());
        log.info("领取店铺优惠券:{}", dto);

        couponService.receiveShopCoupon(dto);
        return Result.ok();
    }

    @GetMapping("/couponPageQuery")
    public Result<PageResult<CouponPageVO>> couponPageQuery(@Validated PageQuery query) {
        log.info("分页查询可使用的优惠卷列表:{}", query);

        return Result.ok(couponService.couponPageQuery(query));
    }

}

