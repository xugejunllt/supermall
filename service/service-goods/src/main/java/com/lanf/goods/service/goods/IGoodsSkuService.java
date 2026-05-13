package com.lanf.goods.service.goods;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.api.goods.model.dto.CalculateOrderTotalAmountDTO;
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
}
