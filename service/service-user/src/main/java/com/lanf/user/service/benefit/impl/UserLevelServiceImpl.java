package com.lanf.user.service.benefit.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.common.utils.IStringUtils;
import com.lanf.constant.enums.BenefitGrantEventEnum;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.user.mapper.UserLevelMapper;
import com.lanf.user.model.bo.CalculationGrowthValueBO;
import com.lanf.user.model.dto.CalculationGrowthValueDTO;
import com.lanf.user.model.entity.UserLevelConfigDO;
import com.lanf.user.model.entity.UserLevelDO;
import com.lanf.user.model.entity.UserLevelDetailDO;
import com.lanf.user.service.benefit.IUserLevelConfigService;
import com.lanf.user.service.benefit.IUserLevelDetailService;
import com.lanf.user.service.benefit.IUserLevelService;
import com.lanf.web.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户会员等级主表 服务实现类
 * </p>
 *
 * @author jarven
 * @since 2025-11-20
 */
@Slf4j
@Service
public class UserLevelServiceImpl extends ServiceImpl<UserLevelMapper, UserLevelDO> implements IUserLevelService {

    @Autowired
    private IUserLevelConfigService userLevelConfigService;

    @Autowired
    private IUserLevelDetailService userLevelDetailService;

    @Override
    public void calculationGrowthValue(CalculationGrowthValueDTO dto) {

        Long userId = dto.getUserId();
       //获取等级配置 key:level
        Map<Integer, UserLevelConfigDO> levelMap = getLevelMap();
        //创建新用户等级
        createUserLevel( userId,levelMap);
        //计算成长值
        CalculationGrowthValueBO growthValueBO = doCalculationGrowthValue(dto, levelMap);
        //更新
        updateUserLevel( growthValueBO);
        if (growthValueBO.getUpgrade()){
            log.info("升级成功,发放权益");
        }
        z
    }

    private void updateUserLevel(CalculationGrowthValueBO growthValueBO){

        Long userId = growthValueBO.getUserId();
        UserLevelDO one = this.lambdaQuery().eq(UserLevelDO::getUserId, userId).one();

        boolean update = this.lambdaUpdate().eq(BaseEntity::getId, one.getId())
                .eq(UserLevelDO::getVersion, one.getVersion())
                .set(UserLevelDO::getLevelId, growthValueBO.getLevelId())
                .set(UserLevelDO::getLevel, growthValueBO.getLevel())
                .set(UserLevelDO::getGrowthValue, growthValueBO.getGrowthValue())
                .update();
        if ( !update){
            throw new BizException("更新失败");
        }
        UserLevelDetailDO userLevelDetailDO = BeanCopyUtils.copyBean(growthValueBO, UserLevelDetailDO.class);
        userLevelDetailService.save(userLevelDetailDO);


    }

    private CalculationGrowthValueBO doCalculationGrowthValue(CalculationGrowthValueDTO dto,Map<Integer, UserLevelConfigDO> levelMap){



        String eventCode = dto.getEventCode();
        Long userId = dto.getUserId();

        BenefitGrantEventEnum byCode = getBenefitGrantEvent( eventCode );
        //
        Integer addValue = byCode.getValue();
        UserLevelDO one = this.lambdaQuery().eq(UserLevelDO::getUserId, userId).one();

        //当前等级
        Integer level = one.getLevel();
        //当前成长值
        Integer growthValue = one.getGrowthValue();
        //累加后成长值
        Integer addGrowthValue = addValue+growthValue;
        //下一等级
        Integer nextLevel = level+1;
        //下一等级配置
        UserLevelConfigDO nextLevelConfigDO = levelMap.get(nextLevel);

        /**
         * 计算是否能升级
         */
        Boolean upgrade = false;
        Integer afterLevel = one.getLevel();
        String levelPrivileges = null;

        //当前等级
        Integer currentLevel = one.getLevel();
        //当前等级ID
        Long levelId = one.getLevelId();


        if (nextLevelConfigDO != null){

            Integer growthValue1 = nextLevelConfigDO.getGrowthValue();

            if (addGrowthValue >=growthValue1 ){
                log.info("进行升级");
                upgrade = true;
                afterLevel = nextLevelConfigDO.getLevel();
                levelPrivileges = nextLevelConfigDO.getLevelPrivileges();
                currentLevel = nextLevelConfigDO.getLevel();
                levelId = nextLevelConfigDO.getId();
            } else {
                log.info("没有达到升级成长值");
            }

        }
        if (nextLevelConfigDO == null){
            log.info("已满级");
        }

        CalculationGrowthValueBO bo = new CalculationGrowthValueBO();
        bo.setUserId(userId);
        bo.setEventName(byCode.getName());
        bo.setEventCode(eventCode);
        bo.setBizId(dto.getBizId());
        bo.setBeforeLevel(one.getLevel());
        bo.setAfterLevel(afterLevel);
        bo.setLevelPrivileges(levelPrivileges);
        bo.setGrowthValue(addValue);
        bo.setAfterTotal(growthValue);
        bo.setCurrentTotal(addGrowthValue);
        bo.setLevel(currentLevel);
        bo.setLevelId(levelId);
        bo.setUpgrade(upgrade);

        return  bo;
    }


    /**
     * key:level
     *
     *
     *
     */

    private Map<Integer,UserLevelConfigDO> getLevelMap(){

        List<UserLevelConfigDO> userLevelConfigDOList = userLevelConfigService.listUserLevelConfig();

        if (IStringUtils.isEmpty(userLevelConfigDOList)){
            throw new BizException("配置不存在");
        }

        return userLevelConfigDOList.stream()
                .collect(Collectors.toMap(UserLevelConfigDO::getLevel, Function.identity()));
    }


    private  BenefitGrantEventEnum getBenefitGrantEvent( String eventCode ){
        BenefitGrantEventEnum byCode = BenefitGrantEventEnum.getByCode(eventCode);
        if (byCode == null){
            log.info("事件不存在");
            throw new BizException("事件不存在");
        }
        return  byCode;
    }
    private void createUserLevel( Long userId,Map<Integer, UserLevelConfigDO> levelMap){

        UserLevelDO one = this.lambdaQuery().eq(UserLevelDO::getUserId, userId).one();
        if ( one == null){
            //从缓存中找到 默认1级
            UserLevelConfigDO configDO = levelMap.get(1);
            if (configDO == null){
                log.info("默认等级配置不存在");
                throw new BizException("默认等级配置不存在");
            }
            UserLevelDO userLevelDO = new UserLevelDO();
            userLevelDO.setUserId(userId);
            userLevelDO.setLevelId(configDO.getId());
            userLevelDO.setGrowthValue(configDO.getGrowthValue());
            this.save(userLevelDO);
        }
    }


}
