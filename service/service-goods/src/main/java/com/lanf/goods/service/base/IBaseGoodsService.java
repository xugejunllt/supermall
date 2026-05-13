package com.lanf.goods.service.base;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.goods.model.dto.AddBaseGoodsDTO;
import com.lanf.goods.model.entity.BaseGoodsDO;
import com.lanf.goods.model.query.BaseGoodsPageQuery;
import com.lanf.goods.model.vo.BaseGoodsByCodeVO;
import com.lanf.goods.model.vo.BaseGoodsBySkuCodeVO;
import com.lanf.goods.model.vo.BaseGoodsPageVO;

import java.util.List;

/**
 * <p>
 * 基础商品 服务类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-09
 */
public interface IBaseGoodsService extends IService<BaseGoodsDO> {

    void  addBaseGoods(AddBaseGoodsDTO baseGoodsAdd);
    PageResult<BaseGoodsPageVO> baseGoodsPageQuery(BaseGoodsPageQuery query);

    BaseGoodsByCodeVO baseGoodsByCodeQuery(String code);

    BaseGoodsBySkuCodeVO baseGoodsBySkuCodeQuery(String skuCode);

    List<BaseGoodsBySkuCodeVO> baseGoodsBySkuCodeBathQuery(List<String> skuCodeList);

}
