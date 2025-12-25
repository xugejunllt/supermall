package com.lanf.welfare.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.constant.exception.BizException;
import com.lanf.welfare.mapper.CouponMapper;
import com.lanf.welfare.model.bo.DeductShopCouponRemainCountCacheBO;
import com.lanf.welfare.model.dto.ReceiveShopCouponDTO;
import com.lanf.welfare.model.entity.CouponDO;
import com.lanf.welfare.model.entity.CouponTemplateDO;
import com.lanf.welfare.model.enums.CouponTemplateStatus;
import com.lanf.welfare.service.ICouponService;
import com.lanf.welfare.service.ICouponTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

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



    @Autowired
    private  CouponCacheService couponCacheService;

    //@DistributedLock(key = "#dto.userId")//防止重复领取
    @Override
    public void receiveShopCoupon(ReceiveShopCouponDTO dto) {

        //validateCouponTemplate( dto);


        Long couponTemplateId = dto.getCouponTemplateId();
        CouponTemplateDO templateDO = couponTemplateService.getById(couponTemplateId);
        DeductShopCouponRemainCountCacheBO bo = couponCacheService.
                deductShopCouponRemainCountCache(templateDO.getShopId(), couponTemplateId);



    }
    private void validateCouponTemplate(ReceiveShopCouponDTO dto){

        Long couponTemplateId = dto.getCouponTemplateId();
        Long shopId = dto.getShopId();
        CouponTemplateDO templateDO = couponTemplateService.getById(couponTemplateId);

        if ( templateDO == null){
            throw new BizException("优惠卷模板不存在");
        }
        if ( !shopId.equals(templateDO.getShopId())){
            throw new BizException("优惠卷模板不属于该店铺");
        }
        if ( !CouponTemplateStatus.PUSH.getCode().equals(templateDO.getStatus())){
            throw new BizException("优惠卷模板未发布");
        }
        Map<String, String> remainCountCache = couponCacheService.getShopCouponRemainCountCache(dto.getShopId());

    }

}
