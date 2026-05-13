package com.lanf.user.service.benefit.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.common.utils.IStringUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.model.enums.BenefitGrantEventEnum;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.user.mapper.UserLevelMapper;
import com.lanf.user.model.bo.CalculationGrowthValue;
import com.lanf.user.model.bo.UserLevel;
import com.lanf.user.model.dto.CalculationGrowthValueDTO;
import com.lanf.user.model.dto.GrantBenefitDTO;
import com.lanf.user.model.dto.LevelBenefitDTO;
import com.lanf.user.model.entity.UserLevelConfigDO;
import com.lanf.user.model.entity.UserLevelDO;
import com.lanf.user.model.entity.UserLevelDetailDO;
import com.lanf.user.service.benefit.IUserLevelConfigService;
import com.lanf.user.service.benefit.IUserLevelDetailService;
import com.lanf.user.service.benefit.IUserLevelService;
import com.lanf.user.service.benefit.manager.BenefitGrantService;
import com.lanf.user.service.benefit.manager.BenefitGrantServiceFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
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

    @Autowired
    private BenefitGrantServiceFactory benefitGrantServiceFactory;

    @Override
    @Transactional
    public void calculationGrowthValue(CalculationGrowthValueDTO dto) {

        log.info("成长值计算开始[{}]", JsonUtils.toJsonString(dto));
        Long userId = dto.getUserId();
        //获取等级配置 key:level
        Map<Integer, UserLevelConfigDO> levelMap = getLevelMap();
        //创建新用户等级
        createUserLevel(userId, levelMap);
        //计算成长值
        CalculationGrowthValue growthValueBO = doCalculationGrowthValue(dto, levelMap);
        //更新
        updateUserLevel(growthValueBO);
        if (growthValueBO.getUpgrade()) {
            log.info("升级成功,发放权益开始");
            grantBenefit(growthValueBO.getLevelPrivileges(), userId);
            log.info("发放权益结束");

        }
        log.info("成长值计算结束");

    }


    private void grantBenefit(String levelPrivileges, Long userId) {

        List<LevelBenefitDTO> list = JsonUtils.toList(levelPrivileges, LevelBenefitDTO.class);

        Set<String> benefitsCode = list.stream()
                .map(LevelBenefitDTO::getCode)
                .collect(Collectors.toSet());
        Set<BenefitGrantService> grantServices = benefitGrantServiceFactory.listBenefitGrantService(benefitsCode);

        GrantBenefitDTO grantBenefitDTO = new GrantBenefitDTO();
        grantBenefitDTO.setUserId(userId);
        grantServices.forEach(a -> {

            a.execute(grantBenefitDTO);

        });

    }

    private void updateUserLevel(CalculationGrowthValue growthValueBO) {

        log.info("更新成长值开始");
        Long userId = growthValueBO.getUserId();
        UserLevelDO one = this.lambdaQuery().eq(UserLevelDO::getUserId, userId).one();
        //乐观锁更新 有乐观锁上层就不需要分布式锁
        boolean update = this.lambdaUpdate().eq(BaseEntity::getId, one.getId())
                .eq(UserLevelDO::getVersion, one.getVersion())
                .set(UserLevelDO::getLevelId, growthValueBO.getLevelId())
                .set(UserLevelDO::getLevel, growthValueBO.getLevel())
                .set(UserLevelDO::getGrowthValue, growthValueBO.getCurrentTotal())
                .set(UserLevelDO::getVersion, one.getVersion() + 1)
                .update();
        if (!update) {
            throw new BizException("更新失败");
        }
        UserLevelDetailDO userLevelDetailDO = BeanCopyUtils.copyBean(growthValueBO, UserLevelDetailDO.class);
        userLevelDetailService.save(userLevelDetailDO);
        log.info("更新成长值结束");

    }

    private CalculationGrowthValue doCalculationGrowthValue(CalculationGrowthValueDTO dto, Map<Integer, UserLevelConfigDO> levelMap) {


        log.info("开始计算");
        String eventCode = dto.getEventCode();
        Long userId = dto.getUserId();

        BenefitGrantEventEnum byCode = getBenefitGrantEvent(eventCode);
        //
        Integer addValue = byCode.getValue();
        UserLevelDO one = this.lambdaQuery().eq(UserLevelDO::getUserId, userId).one();

        //当前等级
        Integer level = one.getLevel();
        //当前成长值
        Integer growthValue = one.getGrowthValue();
        //累加后成长值
        Integer addGrowthValue = addValue + growthValue;
        //下一等级
        Integer nextLevel = level + 1;
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


        if (nextLevelConfigDO != null) {

            log.info("未满级");
            Integer growthValue1 = nextLevelConfigDO.getGrowthValue();

            if (addGrowthValue >= growthValue1) {
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
        if (nextLevelConfigDO == null) {
            log.info("已满级");
        }

        CalculationGrowthValue bo = new CalculationGrowthValue();
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
        log.info("计算结束");
        return bo;
    }


    /**
     * key:level
     */

    private Map<Integer, UserLevelConfigDO> getLevelMap() {

        List<UserLevelConfigDO> userLevelConfigDOList = userLevelConfigService.listUserLevelConfig();

        if (IStringUtils.isEmpty(userLevelConfigDOList)) {
            throw new BizException("配置不存在");
        }

        return userLevelConfigDOList.stream()
                .collect(Collectors.toMap(UserLevelConfigDO::getLevel, Function.identity()));
    }


    private BenefitGrantEventEnum getBenefitGrantEvent(String eventCode) {
        BenefitGrantEventEnum byCode = BenefitGrantEventEnum.getByCode(eventCode);
        if (byCode == null) {
            log.info("事件不存在");
            throw new BizException("事件不存在");
        }
        return byCode;
    }

    private void createUserLevel(Long userId, Map<Integer, UserLevelConfigDO> levelMap) {

        UserLevelDO one = this.lambdaQuery().eq(UserLevelDO::getUserId, userId).one();
        if (one == null) {

            log.info("创建新的用户等级");
            Integer defaultLevel = 1;
            //从缓存中找到 默认1级
            UserLevelConfigDO configDO = levelMap.get(defaultLevel);
            if (configDO == null) {
                log.info("默认等级配置不存在");
                throw new BizException("默认等级配置不存在");
            }
            UserLevelDO userLevelDO = new UserLevelDO();
            userLevelDO.setUserId(userId);
            userLevelDO.setLevelId(configDO.getId());
            userLevelDO.setGrowthValue(configDO.getGrowthValue());
            userLevelDO.setLevel(defaultLevel);
            try {
                this.save(userLevelDO);
            } catch (DuplicateKeyException e) {

                log.warn("重复插入用户等级");

                throw new BizException("重复插入用户等级");
            }

        } else {

            log.info("等级用户已存在,无需创建");
        }
    }

    @Override
    public UserLevel getUserLevel(Long userId) {


        UserLevelDO userLevelDO = getByUserId(userId);
        List<UserLevelConfigDO> configDOS = userLevelConfigService.listUserLevelConfig();
        UserLevelConfigDO userLevelConfigDO = null;

        for (UserLevelConfigDO u : configDOS) {
            if (u.getId().equals(userLevelDO.getLevelId())) {

                userLevelConfigDO = u;
            }
        }
        if (userLevelConfigDO == null) {
            log.warn("未找到等级配置levelId[{}]", userLevelDO.getLevelId());
            throw new BizException("未找到等级配置");
        }

        return BeanCopyUtils.copyBean(userLevelConfigDO, UserLevel.class);
    }

    private UserLevelDO getByUserId(Long userId) {

        return this.lambdaQuery().eq(UserLevelDO::getUserId, userId).one();
    }
}
