package com.lanf.user.service.benefit.manager;


import com.lanf.user.model.dto.GrantBenefitDTO;
import org.springframework.stereotype.Component;

/**
 * 钱包余额发放权益
 *@Component 避免权益没有开放 被滥用 手动创建
 */

public class WalletBalanceBenefitGrantServiceImpl implements BenefitGrantService{


    @Override
    public void execute(GrantBenefitDTO benefitDTO) {
        //调用钱包服务 发送钱包余额
    }


}
