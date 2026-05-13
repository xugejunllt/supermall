package com.lanf.goods.service.goods;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.constant.model.query.PageQuery;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.goods.model.dto.GoodsCategoryAddDTO;
import com.lanf.goods.model.entity.GoodsCategoryDO;
import com.lanf.goods.model.vo.GoodsCategoryPageVO;

/**
 * <p>
 * 商品分类 服务类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-11
 */
public interface IGoodsCategoryService extends IService<GoodsCategoryDO> {

    void goodsCategoryAdd(GoodsCategoryAddDTO dto);

    PageResult<GoodsCategoryPageVO> goodsCategoryPage(PageQuery query);
}
