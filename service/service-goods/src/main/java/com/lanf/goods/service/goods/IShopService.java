package com.lanf.goods.service.goods;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.constant.model.query.PageQuery;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.goods.model.dto.AddShopDTO;
import com.lanf.goods.model.entity.ShopDO;
import com.lanf.goods.model.vo.ShopListVO;
import com.lanf.goods.model.vo.ShopPageVO;

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


    void  addShop(AddShopDTO dto);
    PageResult<ShopPageVO> shopPageQuery(PageQuery query);

    List<ShopListVO> shopListQuery();
}
