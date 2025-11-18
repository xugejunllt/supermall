package com.lanf.user.service.benefit.manager;

import com.lanf.user.model.dto.GrantBenefitDTO;

/**
 * 权益发放接口
 */
public interface BenefitGrantService {

    /**
     * 发放权益
     *
     */
    void execute(GrantBenefitDTO benefitDTO);
}
