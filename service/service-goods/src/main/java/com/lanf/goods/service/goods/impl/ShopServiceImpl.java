package com.lanf.goods.service.goods.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.constant.model.query.PageQuery;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.goods.mapper.ShopMapper;
import com.lanf.goods.model.entity.ShopDO;
import com.lanf.goods.service.goods.IShopService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 店铺信息 服务实现类
 * </p>
 *
 * @author jarven
 * @since 2025-11-30
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, ShopDO> implements IShopService {

    @Override
    public void addShop(ShopDTO dto) {
        ShopDO shop = BeanCopyUtils.copyBean(dto, ShopDO.class);
        this.save(shop);

    }

    @Override
    public PageResult<ShopDO> shopPage(PageQuery query) {
        IPage<ShopDO> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<ShopDO> pageResult = this.lambdaQuery().
                orderByDesc(ShopDO::getUpdateTime)
                .page(page);

        PageResult<ShopDO> result = new PageResult<>();
        result.setTotal(pageResult.getTotal());
        result.setSize(pageResult.getSize());
        result.setRecords(pageResult.getRecords());
        return result;
    }

    @Override
    public List<ShopDO> shopList() {
        return this.list();
    }
}
