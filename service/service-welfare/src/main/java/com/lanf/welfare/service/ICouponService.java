package com.lanf.welfare.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.welfare.model.dto.ReceiveShopCouponDTO;
import com.lanf.welfare.model.entity.CouponDO;

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

}
