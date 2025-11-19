package com.lanf.user.service.benefit.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.lock.aop.DistributedLock;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.mybatis.base.PageResult;
import com.lanf.security.utils.UserUtils;
import com.lanf.user.mapper.BenefitMapper;
import com.lanf.user.model.dto.CreateBenefitDTO;
import com.lanf.user.model.entity.BenefitDO;
import com.lanf.user.model.enums.BenefitCodeEnum;
import com.lanf.user.model.query.BenefitPageQuery;
import com.lanf.user.service.benefit.IBenefitService;
import com.lanf.user.service.benefit.manager.BenefitGrantServiceFactory;
import com.lanf.web.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 权益表
 * 服务实现类
 * </p>
 *
 * @author jarven
 * @since 2025-11-19
 */
@Slf4j
@Service
public class BenefitServiceImpl extends ServiceImpl<BenefitMapper, BenefitDO> implements IBenefitService {

    @Autowired
    private BenefitGrantServiceFactory benefitGrantServiceFactory;

    @Override
    @DistributedLock(key = "#dto.code")
    public void createBenefit(CreateBenefitDTO dto) {

        validateCreateBenefit(dto);

        BenefitDO benefitDO = BeanCopyUtils.copyBean(dto, BenefitDO.class);
        benefitDO.setStatus(0);
        this.save(benefitDO);

    }


    private void validateCreateBenefit(CreateBenefitDTO dto) {

        String code = dto.getCode();
        boolean includeCode = BenefitCodeEnum.includeCode(code);
        if (!includeCode) {
            log.info("不支持的权益code");
            throw new BizException("不支持的权益code");
        }
        BenefitDO benefitDO = this.lambdaQuery().eq(BenefitDO::getCode, code).one();
        if (benefitDO != null) {
            log.info("权益已存在");
            throw new BizException("权益已存在");
        }


    }

    @Override
    @Transactional//使用事务 DB与添加service同时成功或失败
    @DistributedLock(key = "#id")
    public void useBenefit(Long id) {

        validateUseBenefit(id);
        //更新

        boolean update = this.lambdaUpdate()
                .eq(BaseEntity::getId, id)
                .set(BenefitDO::getStatus, 1)
                .update();
        if (!update) {
            log.info("更新失败");
            throw new BizException("更新失败");
        }
        //添加权益服务
        BenefitDO benefitDO = this.getById(id);
        benefitGrantServiceFactory.addBenefitGrantService(benefitDO.getCode());

    }


    private void validateUseBenefit(Long id) {
        BenefitDO benefitDO = this.getById(id);
        if (benefitDO == null) {
            log.info("权益不存在");
            throw new BizException("权益不存在");
        }
        Integer status = benefitDO.getStatus();
        if (status != 0) {

            log.info("该状态不允许使用");
            throw new BizException("该状态不允许使用");
        }

    }

    @Override
    @Transactional//使用事务 DB与添加service同时成功或失败
    @DistributedLock(key = "#id")
    public void disableBenefit(Long id) {

        validateDisableBenefit(id);
        //更新
        boolean update = update = this.lambdaUpdate()
                .eq(BaseEntity::getId, id)
                .set(BenefitDO::getStatus, 0)
                .update();
        if (!update) {
            log.info("更新失败");
            throw new BizException("更新失败");
        }
        BenefitDO benefitDO = this.getById(id);
        benefitGrantServiceFactory.removeBenefitGrantService(benefitDO.getCode());

    }



    private void validateDisableBenefit(Long id) {
        BenefitDO benefitDO = this.getById(id);
        if (benefitDO == null) {
            log.info("权益不存在");
            throw new BizException("权益不存在");
        }
        Integer status = benefitDO.getStatus();

        if (status != 1) {
            log.info("该状态不允许禁用");
            throw new BizException("该状态不允许禁用");
        }


    }
    @Override
    public List<String> listUseBenefitCode() {

        List<BenefitDO> benefitDOList = this.lambdaQuery().eq(BenefitDO::getStatus, 1).list();

        List<String> listCode = new ArrayList<>();

        for (BenefitDO benefitDO : benefitDOList){
            listCode.add(benefitDO.getCode());
        }

        return listCode;
    }

    @Override
    public PageResult<BenefitDO> pageBenefit(BenefitPageQuery query) {

        IPage<BenefitDO> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<BenefitDO> pageResult = this.lambdaQuery().
                orderByDesc(BaseEntity::getUpdateTime)
                .page(page);

        return PageResult.toPageResult(pageResult);
    }

}
