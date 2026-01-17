package com.lanf.welfare.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.welfare.model.dto.CalculateDiscountAmountDTO;
import com.lanf.welfare.model.dto.ReceiveShopCouponDTO;
import com.lanf.welfare.model.dto.UseMultipleCouponDTO;
import com.lanf.welfare.model.entity.CouponDO;
import com.lanf.welfare.model.vo.CalculateDiscountAmountVO;

/**
 * <p>
 * 优惠券 服务类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-08-01
 */
public interface ICouponService extends IService<CouponDO> {

    /**
     * 领取店铺优惠卷
     */
    void  receiveShopCoupon(ReceiveShopCouponDTO dto);

    /**
     * 计算优惠券金额
     *
     *
     */
    CalculateDiscountAmountVO calculateDiscountAmount(CalculateDiscountAmountDTO dto);

    /**
     * 使用多张优惠卷
     *
     */

    CalculateDiscountAmountVO useMultipleCoupon(UseMultipleCouponDTO dto);
}
