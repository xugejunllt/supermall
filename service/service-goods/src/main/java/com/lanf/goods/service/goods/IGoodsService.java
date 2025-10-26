package com.lanf.goods.service.goods;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.goods.model.dto.GoodsAddDTO;
import com.lanf.goods.model.dto.CheckAndQueryGoodsDTO;
import com.lanf.goods.model.dto.UpDownStatusDTO;
import com.lanf.goods.model.entity.GoodsDO;
import com.lanf.goods.model.query.GoodsPageQuery;
import com.lanf.goods.model.query.UserGoodsPageQuery;
import com.lanf.goods.model.vo.*;
import com.lanf.mybatis.base.PageResult;

import java.util.List;

/**
 * <p>
 * 基础商品 服务类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-11
 */
public interface IGoodsService extends IService<GoodsDO> {

    void  goodsAdd(GoodsAddDTO dto);

    PageResult<GoodsPageVO> goodsPage(GoodsPageQuery query);

    GoodsDetailVO goodsDetail(Long id);

    PageResult<UserGoodsPageVO> userGoodsPage(UserGoodsPageQuery query);

    UserGoodsDetailVO userGoodsDetail(Long id);
    SkuDetailVO  queryBySkuId(Long skuId);
    List<ApiGoodsSkuVO> queryBySkuCode(List<String> skuCode);

    /**
     *
     * 上下架商品
     *
     */
    void upDownStatus(UpDownStatusDTO dto);

    /**
     * 校验和查询商品信息
     *
     */
    ApiGoodsSkuVO  checkAndQueryGoods(CheckAndQueryGoodsDTO dto);
}
