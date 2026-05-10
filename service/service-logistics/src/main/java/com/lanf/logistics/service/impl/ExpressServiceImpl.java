package com.lanf.logistics.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.logistics.mapper.ExpressMapper;
import com.lanf.logistics.model.dto.ExpressAddDTO;
import com.lanf.logistics.model.entity.ExpressDO;
import com.lanf.logistics.service.IExpressService;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.constant.web.PageQuery;
import com.lanf.constant.web.PageResult;
import com.lanf.security.utils.UserUtils;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-17
 */
@Service
public class ExpressServiceImpl extends ServiceImpl<ExpressMapper, ExpressDO> implements IExpressService {

    @Override
    public void expressAdd(ExpressAddDTO dto) {

        ExpressDO expressDO = new ExpressDO();
        BeanCopyUtils.copy(dto, expressDO);
        expressDO.setBusinessId(UserUtils.getBusinessId());
        this.save(expressDO);

    }

    @Override
    public PageResult<ExpressDO> expressPage(PageQuery query) {

        IPage<ExpressDO> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<ExpressDO> companyPage = this.lambdaQuery().
                eq(ExpressDO::getBusinessId, UserUtils.getBusinessId()).
                orderByDesc(BaseEntity::getUpdateTime)
                .page(page);

        return PageResult.toPageResult(companyPage);
    }



}
