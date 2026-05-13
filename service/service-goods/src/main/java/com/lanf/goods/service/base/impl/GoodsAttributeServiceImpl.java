package com.lanf.goods.service.base.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.constant.context.MerchantIdContext;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.model.query.PageQuery;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.goods.mapper.GoodsAttributeMapper;
import com.lanf.api.goods.model.dto.AddGoodsAttributeDTO;
import com.lanf.api.goods.model.dto.UpdateGoodsAttributeDTO;
import com.lanf.goods.model.entity.GoodsAttributeDO;
import com.lanf.api.goods.model.vo.GoodsAttributeDetailVO;
import com.lanf.api.goods.model.vo.GoodsAttributeListVO;
import com.lanf.api.goods.model.vo.GoodsAttributePageVO;
import com.lanf.goods.service.base.IGoodsAttributeService;
import com.lanf.mybatis.base.BaseEntity;
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
    public void addGoodsAttribute(AddGoodsAttributeDTO dto) {

        GoodsAttributeDO one = this.lambdaQuery().
                eq(GoodsAttributeDO::getAttribute, dto.getAttribute())
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
    public PageResult<GoodsAttributePageVO> goodsAttributePageQuery(PageQuery query) {

        IPage<GoodsAttributeDO> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<GoodsAttributeDO> result = this.lambdaQuery().
                eq(GoodsAttributeDO::getTenantId, MerchantIdContext.getMerchantId()).
                orderByDesc(BaseEntity::getUpdateTime)
                .page(page);
        if (result.getRecords().isEmpty()){

            return PageResult.emptyResult();
        }
        PageResult<GoodsAttributePageVO> resultVo = new PageResult<>();
        resultVo.setTotal(result.getTotal());
        resultVo.setSize(result.getSize());
        resultVo.setRecords(BeanCopyUtils.copyBeanList(result.getRecords(), GoodsAttributePageVO.class));
        return resultVo;
    }

    @Override
    public void updateGoodsAttribute(UpdateGoodsAttributeDTO dto) {

        GoodsAttributeDO GoodsAttributeUpdate = new GoodsAttributeDO();
        GoodsAttributeUpdate.setId(dto.getId());
        GoodsAttributeUpdate.setAttributeValue(dto.getAttributeValue());
        GoodsAttributeUpdate.setSort(dto.getSort());
        this.updateById(GoodsAttributeUpdate);

    }

    @Override
    public List<GoodsAttributeListVO> goodsAttributeListQuery() {

        List<GoodsAttributeDO> list = this.lambdaQuery().eq(GoodsAttributeDO::getTenantId, MerchantIdContext.getMerchantId()).list();
        if (list.isEmpty()){
            return null;
        }

        return BeanCopyUtils.copyBeanList(list, GoodsAttributeListVO.class);
    }

    @Override
    public GoodsAttributeDetailVO goodsAttributeDetailQuery(Long id) {
        GoodsAttributeDO byId = this.getById(id);
        if (byId == null){
            return null;
        }
        return BeanCopyUtils.copyBean(byId, GoodsAttributeDetailVO.class);
    }

}
