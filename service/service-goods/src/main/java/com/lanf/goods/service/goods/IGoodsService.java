package com.lanf.goods.service.goods;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.goods.model.dto.AddGoodsDTO;
import com.lanf.goods.model.dto.CheckAndQueryGoodsDTO;
import com.lanf.goods.model.dto.UpDownStatusDTO;
import com.lanf.goods.model.dto.UpGoodsDTO;
import com.lanf.goods.model.entity.GoodsDO;
import com.lanf.goods.model.query.GoodsPageQuery;
import com.lanf.goods.model.vo.*;

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

    void  addGoods(AddGoodsDTO dto);

    PageResult<GoodsPageVO> goodsPageQuery(GoodsPageQuery query);

    GoodsDetailVO goodsDetailQuery(Long id);


    UserGoodsDetailVO userGoodsDetail(Long id);
    SkuDetailVO  queryBySkuId(Long skuId);
    List<ApiGoodsSkuVO> queryBySkuCode(List<String> skuCode);



    /**
     * 校验和查询商品信息
     *
     */
    ApiGoodsSkuVO  checkAndQueryGoods(CheckAndQueryGoodsDTO dto);

    /**
     * 上架商品
     *
     */
    void upGoods(UpGoodsDTO dto);
}
