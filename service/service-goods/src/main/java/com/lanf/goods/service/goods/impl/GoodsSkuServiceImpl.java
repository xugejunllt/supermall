package com.lanf.goods.service.goods.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BigDecimalUtil;
import com.lanf.goods.mapper.GoodsSkuMapper;
import com.lanf.goods.model.dto.CalculateOrderAmountDTO;
import com.lanf.goods.model.entity.GoodsSkuDO;
import com.lanf.goods.model.vo.CalculateOrderAmountVO;
import com.lanf.goods.service.goods.IGoodsSkuService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-11
 */
@Service
public class GoodsSkuServiceImpl extends ServiceImpl<GoodsSkuMapper, GoodsSkuDO> implements IGoodsSkuService {

    @Override
    public CalculateOrderAmountVO calculateOrderAmount(CalculateOrderAmountDTO dto) {

        Long skuId = dto.getSkuId();
        GoodsSkuDO goodsSkuDO = this.getById(skuId);
        BigDecimal price = goodsSkuDO.getPrice();
        Integer quantity = dto.getQuantity();
        //订单总金额
        BigDecimal totalAmount = BigDecimalUtil.multiply(price, BigDecimal.valueOf(quantity));
        z

        return null;
    }
}
