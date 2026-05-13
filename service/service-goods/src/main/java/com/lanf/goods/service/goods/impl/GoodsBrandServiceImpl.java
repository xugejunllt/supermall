package com.lanf.goods.service.goods.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.model.query.PageQuery;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.goods.mapper.GoodsBrandMapper;
import com.lanf.goods.model.dto.GoodsBrandAddDTO;
import com.lanf.goods.model.entity.GoodsBrandDO;
import com.lanf.goods.service.goods.IGoodsBrandService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 商品品牌 服务实现类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-11
 */
@Service
public class GoodsBrandServiceImpl extends ServiceImpl<GoodsBrandMapper, GoodsBrandDO> implements IGoodsBrandService {

    @Override
    public void goodsBrandAdd(GoodsBrandAddDTO dto) {

        String name = dto.getName();
        GoodsBrandDO one = this.lambdaQuery().ge(GoodsBrandDO::getName, name).one();
        if (one != null){
            throw new BizException("品牌已存在");
        }
        GoodsBrandDO goodsBrandDO = new GoodsBrandDO();
        BeanCopyUtils.copy(dto, goodsBrandDO);
        this.save(goodsBrandDO);

    }

    @Override
    public PageResult<GoodsBrandDO> goodsBrandPage(PageQuery query) {

        IPage<GoodsBrandDO> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<GoodsBrandDO> pageResult = this.lambdaQuery().
                orderByDesc(GoodsBrandDO::getUpdateTime)
                .page(page);

        PageResult<GoodsBrandDO> result = new PageResult<>();
        result.setTotal(pageResult.getTotal());
        result.setSize(pageResult.getSize());
        result.setRecords(pageResult.getRecords());

        return result;
    }

    @Override
    public List<GoodsBrandDO> goodsBrandList() {

        return this.lambdaQuery().list();
    }
}
