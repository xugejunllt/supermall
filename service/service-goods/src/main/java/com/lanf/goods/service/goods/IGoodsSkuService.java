package com.lanf.goods.service.goods;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.api.goods.model.dto.CalculateOrderTotalAmountDTO;
import com.lanf.api.goods.model.query.GoodsSkuPageQuery;
import com.lanf.api.goods.model.vo.GoodsSkuPageVO;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.goods.model.entity.GoodsSkuDO;
import com.lanf.api.goods.model.vo.CalculateOrderTotalAmountVO;

/**
 *
 *  服务类
 *
 *
 *
 */
public interface IGoodsSkuService extends IService<GoodsSkuDO> {


    /**
     *  计算商品金额
     */
    
    CalculateOrderTotalAmountVO calculateOrderTotalAmount(CalculateOrderTotalAmountDTO dto);

    /**
     * 分页查询商品SKU列表
     * @param query 查询条件
     * @return 分页结果
     */
    PageResult<GoodsSkuPageVO> goodsSkuPageQuery(GoodsSkuPageQuery query);
}
