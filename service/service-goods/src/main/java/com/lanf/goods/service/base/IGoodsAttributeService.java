package com.lanf.goods.service.base;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.constant.model.query.PageQuery;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.goods.model.dto.GoodsAttributeAddDTO;
import com.lanf.goods.model.dto.GoodsAttributeUpdateDTO;
import com.lanf.goods.model.entity.GoodsAttributeDO;

import java.util.List;

/**
 * <p>
 * 商品属性 服务类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-07-06
 */
public interface IGoodsAttributeService extends IService<GoodsAttributeDO> {

    void  goodsAttributeAdd(GoodsAttributeAddDTO dto);

    PageResult<GoodsAttributeDO> goodsAttributePage(PageQuery query);

    void  goodsAttributeUpdate(GoodsAttributeUpdateDTO dto);

    List<GoodsAttributeDO> goodsAttributeList();

    GoodsAttributeDO detail(Long id);
}
