package com.lanf.user.service.benefit.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.cache.aop.DistributedLock;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.user.mapper.BenefitMapper;
import com.lanf.user.model.dto.AddBenefitDTO;
import com.lanf.user.model.dto.DisableBenefitDTO;
import com.lanf.user.model.dto.UseBenefitDTO;
import com.lanf.user.model.entity.BenefitDO;
import com.lanf.user.model.enums.BenefitCodeEnum;
import com.lanf.user.model.query.BenefitPageQuery;
import com.lanf.user.service.benefit.IBenefitService;
import com.lanf.user.service.benefit.manager.BenefitGrantServiceFactory;
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
    public void addBenefit(AddBenefitDTO dto) {

        validateCreateBenefit(dto);

        BenefitDO benefitDO = BeanCopyUtils.copyBean(dto, BenefitDO.class);
        benefitDO.setStatus(0);
        this.save(benefitDO);

    }


    private void validateCreateBenefit(AddBenefitDTO dto) {

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
    @Transactional
    @DistributedLock(key = "#dto.id")
    public void useBenefit(UseBenefitDTO dto) {

        Long id = dto.getId();
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
    @Transactional
    @DistributedLock(key = "#dto.idid")
    public void disableBenefit(DisableBenefitDTO dto) {

        Long id = dto.getId();
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
    public PageResult<BenefitDO> benefitPageQuery(BenefitPageQuery query) {

        IPage<BenefitDO> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<BenefitDO> pageResult = this.lambdaQuery().
                orderByDesc(BaseEntity::getUpdateTime)
                .page(page);
        PageResult<BenefitDO> result = new PageResult<>();
        result.setTotal(pageResult.getTotal());
        result.setSize(pageResult.getSize());
        result.setRecords(pageResult.getRecords());
        return result;
    }

}
