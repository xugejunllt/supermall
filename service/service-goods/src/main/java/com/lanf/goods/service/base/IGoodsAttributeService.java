package com.lanf.goods.service.base;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.constant.model.query.PageQuery;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.goods.model.dto.AddGoodsAttributeDTO;
import com.lanf.goods.model.dto.UpdateGoodsAttributeDTO;
import com.lanf.goods.model.entity.GoodsAttributeDO;
import com.lanf.goods.model.vo.GoodsAttributeDetailVO;
import com.lanf.goods.model.vo.GoodsAttributeListVO;
import com.lanf.goods.model.vo.GoodsAttributePageVO;

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

    void  addGoodsAttribute(AddGoodsAttributeDTO dto);

    PageResult<GoodsAttributePageVO> goodsAttributePageQuery(PageQuery query);

    void  updateGoodsAttribute(UpdateGoodsAttributeDTO dto);

    List<GoodsAttributeListVO> goodsAttributeListQuery();

    GoodsAttributeDetailVO goodsAttributeDetailQuery(Long id);
}
