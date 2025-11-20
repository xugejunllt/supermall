package com.lanf.user.service.benefit.manager;


import com.lanf.user.model.dto.GrantBenefitDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 红包发放权益
 *
 *
 */
@Slf4j
@Component
public class CouponBenefitGrantServiceImpl implements BenefitGrantService{


    @Override
    public void execute(GrantBenefitDTO benefitDTO) {
        //调用优惠卷服务 发送优惠卷
        log.info("发送优惠卷开始");
    }

}
