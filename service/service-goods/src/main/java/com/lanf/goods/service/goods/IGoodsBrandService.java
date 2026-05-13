package com.lanf.goods.service.goods;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.constant.model.query.PageQuery;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.api.goods.model.dto.AddGoodsBrandDTO;
import com.lanf.goods.model.entity.GoodsBrandDO;
import com.lanf.api.goods.model.vo.GoodsBrandListVO;
import com.lanf.api.goods.model.vo.GoodsBrandPageVO;

import java.util.List;

/**
 * <p>
 * 商品品牌 服务类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-11
 */
public interface IGoodsBrandService extends IService<GoodsBrandDO> {

    void  addGoodsBrand(AddGoodsBrandDTO dto);
    PageResult<GoodsBrandPageVO> goodsBrandPageQuery(PageQuery query);

    List<GoodsBrandListVO> goodsBrandListQuery();

}
