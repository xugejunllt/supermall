package com.lanf.goods.service.base.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.goods.mapper.GoodsAttributeMapper;
import com.lanf.goods.model.dto.GoodsAttributeAddDTO;
import com.lanf.goods.model.dto.GoodsAttributeUpdateDTO;
import com.lanf.goods.model.entity.GoodsAttributeDO;
import com.lanf.goods.service.base.IGoodsAttributeService;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.mybatis.base.PageQuery;
import com.lanf.mybatis.base.PageResult;
import com.lanf.security.utils.MerchantIdContext;
import com.lanf.constant.exception.BizException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 商品属性 服务实现类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-07-06
 */
@Service
public class GoodsAttributeServiceImpl extends ServiceImpl<GoodsAttributeMapper, GoodsAttributeDO> implements IGoodsAttributeService {


    @Override
    public void goodsAttributeAdd(GoodsAttributeAddDTO dto) {

        GoodsAttributeDO one = this.lambdaQuery().
                eq(GoodsAttributeDO::getAttribute, dto.getAttribute()).
                eq(GoodsAttributeDO::getTenantId, MerchantIdContext.getMerchantId())
                .one();
        if (one != null) {
            throw new BizException("属性已存在");
        }

        GoodsAttributeDO goodsAttributeDO = new GoodsAttributeDO();
        BeanCopyUtils.copy(dto, goodsAttributeDO);
        goodsAttributeDO.setTenantId(MerchantIdContext.getMerchantId());
        this.save(goodsAttributeDO);

    }

    @Override
    public PageResult<GoodsAttributeDO> goodsAttributePage(PageQuery query) {

        IPage<GoodsAttributeDO> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<GoodsAttributeDO> result = this.lambdaQuery().
                eq(GoodsAttributeDO::getTenantId, MerchantIdContext.getMerchantId()).
                orderByDesc(BaseEntity::getUpdateTime)
                .page(page);

        return PageResult.toPageResult(result);
    }

    @Override
    public void goodsAttributeUpdate(GoodsAttributeUpdateDTO dto) {

        GoodsAttributeDO GoodsAttributeUpdate = new GoodsAttributeDO();
        GoodsAttributeUpdate.setId(dto.getId());
        GoodsAttributeUpdate.setAttributeValue(dto.getAttributeValue());
        GoodsAttributeUpdate.setSort(dto.getSort());
        this.updateById(GoodsAttributeUpdate);

    }

    @Override
    public List<GoodsAttributeDO> goodsAttributeList() {

        return this.lambdaQuery().eq(GoodsAttributeDO::getTenantId, MerchantIdContext.getMerchantId()).list();
    }

    @Override
    public GoodsAttributeDO detail(Long id) {

        return this.getById(id);
    }

}
