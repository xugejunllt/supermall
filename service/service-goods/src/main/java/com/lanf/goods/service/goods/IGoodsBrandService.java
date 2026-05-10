package com.lanf.goods.service.goods;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.goods.model.dto.GoodsBrandAddDTO;
import com.lanf.goods.model.entity.GoodsBrandDO;
import com.lanf.constant.web.PageQuery;
import com.lanf.constant.web.PageResult;

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

    void  goodsBrandAdd(GoodsBrandAddDTO dto);
    PageResult<GoodsBrandDO> goodsBrandPage(PageQuery query);

    List<GoodsBrandDO> goodsBrandList();

}
