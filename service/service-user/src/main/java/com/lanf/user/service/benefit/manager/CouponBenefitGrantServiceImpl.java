package com.lanf.user.service.benefit.manager;


import com.lanf.user.model.dto.GrantBenefitDTO;
import org.springframework.stereotype.Component;

/**
 * 红包发放权益
 *
 *@Component 避免权益没有开放 被滥用 手动创建
 */

public class CouponBenefitGrantServiceImpl implements BenefitGrantService{


    @Override
    public void execute(GrantBenefitDTO benefitDTO) {
        //调用优惠卷服务 发送优惠卷
    }
}
