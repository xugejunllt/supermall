package com.lanf.goods.service.goods.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.constant.model.query.PageQuery;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.goods.mapper.ShopMapper;
import com.lanf.goods.model.dto.AddShopDTO;
import com.lanf.goods.model.entity.ShopDO;
import com.lanf.goods.model.vo.ShopListVO;
import com.lanf.goods.model.vo.ShopPageVO;
import com.lanf.goods.service.goods.IShopService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    public void addShop(AddShopDTO dto) {
        ShopDO shop = BeanCopyUtils.copyBean(dto, ShopDO.class);
        this.save(shop);

    }

    @Override
    public PageResult<ShopPageVO> shopPageQuery(PageQuery query) {
        IPage<ShopDO> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<ShopDO> pageResult = this.lambdaQuery().
                orderByDesc(ShopDO::getUpdateTime)
                .page(page);
        if (pageResult.getRecords().isEmpty()){

            return PageResult.emptyResult();
        }

        PageResult<ShopPageVO> result = new PageResult<>();
        result.setTotal(pageResult.getTotal());
        result.setSize(pageResult.getSize());
        result.setRecords(BeanCopyUtils.copyBeanList(pageResult.getRecords(), ShopPageVO.class));
        return result;
    }

    @Override
    public List<ShopListVO> shopListQuery() {

        List<ShopDO> shopDOList = this.list();
        if (shopDOList.isEmpty()){
            return new ArrayList<>();
        }
        return BeanCopyUtils.copyBeanList(shopDOList, ShopListVO.class);
    }
}
