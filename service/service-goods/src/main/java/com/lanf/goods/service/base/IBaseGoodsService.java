package com.lanf.goods.service.base;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.goods.model.dto.BaseGoodsAddDTO;
import com.lanf.goods.model.entity.BaseGoodsDO;
import com.lanf.goods.model.query.BaseGoodsPageQuery;
import com.lanf.goods.model.vo.BaseGoodsByCodeQueryVO;
import com.lanf.goods.model.vo.BaseGoodsBySkuCodeQueryVO;
import com.lanf.goods.model.vo.BaseGoodsPageVO;
import com.lanf.mybatis.base.PageResult;

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

    void  baseGoodsAdd(BaseGoodsAddDTO baseGoodsAdd);
    PageResult<BaseGoodsPageVO> baseGoodsPage(BaseGoodsPageQuery query);

    BaseGoodsByCodeQueryVO baseGoodsByCodeQuery(String code);

    BaseGoodsBySkuCodeQueryVO baseGoodsBySkuCodeQuery(String skuCode);

    List<BaseGoodsBySkuCodeQueryVO> baseGoodsBySkuCodeBathQuery(List<String> skuCodeList);

}
