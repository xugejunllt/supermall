package com.lanf.goods.service.goods;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.constant.model.query.PageQuery;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.goods.model.dto.ShopDTO;
import com.lanf.goods.model.entity.ShopDO;

import java.util.List;

/**
 * <p>
 * 店铺信息 服务类
 * </p>
 *
 * @author jarven
 * @since 2025-11-30
 */
public interface IShopService extends IService<ShopDO> {


    void  addShop(ShopDTO dto);
    PageResult<ShopDO> shopPage(PageQuery query);

    List<ShopDO> shopList();
}
