package com.lanf.user.service.benefit;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.user.model.dto.CalculationGrowthValueDTO;
import com.lanf.user.model.entity.UserLevelDO;

/**
 * <p>
 * 用户会员等级主表 服务类
 * </p>
 *
 * @author jarven
 * @since 2025-11-20
 */
public interface IUserLevelService extends IService<UserLevelDO> {

    /**
     * 计算成长值
     * @param dto
     */
    void  calculationGrowthValue(CalculationGrowthValueDTO dto);

}
