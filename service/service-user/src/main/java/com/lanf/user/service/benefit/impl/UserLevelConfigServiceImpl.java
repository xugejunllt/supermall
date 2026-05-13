package com.lanf.user.service.benefit.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.cache.aop.DistributedLock;
import com.lanf.user.mapper.UserLevelConfigMapper;
import com.lanf.user.model.dto.AddUserLevelConfigDTO;
import com.lanf.user.model.dto.LevelBenefitDTO;
import com.lanf.user.model.entity.UserLevelConfigDO;
import com.lanf.user.model.enums.BenefitCodeEnum;
import com.lanf.user.service.benefit.IUserLevelConfigService;
import com.lanf.constant.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 会员等级配置表 服务实现类
 * </p>
 *
 * @author jarven
 * @since 2025-11-20
 */
@Slf4j
@Service
public class UserLevelConfigServiceImpl extends ServiceImpl<UserLevelConfigMapper, UserLevelConfigDO> implements IUserLevelConfigService {

    @Override
    @DistributedLock(key = "#dto.level")
    public void addUserLevelConfig(AddUserLevelConfigDTO dto) {

        validateCreateUserLevel( dto);

        UserLevelConfigDO levelConfigDO = BeanCopyUtils.copyBean(dto, UserLevelConfigDO.class);
        List<LevelBenefitDTO> levelPrivileges = dto.getLevelPrivileges();
        String jsonString = JsonUtils.toJsonString(levelPrivileges);
        levelConfigDO.setLevelPrivileges(jsonString);

        this.save(levelConfigDO);

    }


    private void validateCreateUserLevel(AddUserLevelConfigDTO dto){
        UserLevelConfigDO configDO = this.lambdaQuery().eq(UserLevelConfigDO::getLevel, dto.getLevel()).one();
        if (configDO != null){

            throw new BizException("等级已存在");
        }
        List<LevelBenefitDTO> levelPrivileges = dto.getLevelPrivileges();
        levelPrivileges.forEach(a->{

            if ( !BenefitCodeEnum.includeCode(a.getCode())){
                log.info("权益code不存在");
                throw new BizException("权益code不存在");
            }

        });

    }

    @Override
    public List<UserLevelConfigDO> listUserLevelConfig() {


        return this.lambdaQuery().list();
    }

}
