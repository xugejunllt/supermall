package com.lanf.goods.service.goods;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.goods.model.dto.CalculateOrderAmountDTO;
import com.lanf.goods.model.entity.GoodsSkuDO;
import com.lanf.goods.model.vo.CalculateOrderAmountVO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-11
 */
public interface IGoodsSkuService extends IService<GoodsSkuDO> {


    /**
     *  订单金额计算
     *
     *
     */
    CalculateOrderAmountVO calculateOrderAmount(CalculateOrderAmountDTO dto);

}
