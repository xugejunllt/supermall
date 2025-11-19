package com.lanf.user.service.benefit;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.user.model.dto.CreateUserLevelConfigDTO;
import com.lanf.user.model.entity.UserLevelConfigDO;

import java.util.List;

/**
 * <p>
 * 会员等级配置表 服务类
 * </p>
 *
 * @author jarven
 * @since 2025-11-20
 */
public interface IUserLevelConfigService extends IService<UserLevelConfigDO> {


    void createUserLevelConfig(CreateUserLevelConfigDTO dto);

    /**
     * 查询所有配置
     * @return
     */
    List<UserLevelConfigDO> listUserLevelConfig();

}
