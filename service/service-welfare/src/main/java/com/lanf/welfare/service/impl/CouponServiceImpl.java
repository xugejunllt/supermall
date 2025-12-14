package com.lanf.welfare.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.welfare.mapper.CouponMapper;
import com.lanf.welfare.model.dto.ReceiveShopCouponDTO;
import com.lanf.welfare.model.entity.CouponDO;
import com.lanf.welfare.service.ICouponService;
import com.lanf.welfare.service.ICouponTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 优惠券 服务实现类
 *
 *
 * @since 2024-08-01
 */
@Service
public class CouponServiceImpl extends ServiceImpl<CouponMapper, CouponDO> implements ICouponService {

    @Autowired
    private ICouponTemplateService couponTemplateService;


    @Override
    public void receiveShopCoupon(ReceiveShopCouponDTO dto) {

    }
}
