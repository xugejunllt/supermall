package com.lanf.goods.service.goods.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.goods.mapper.GoodsSkuMapper;
import com.lanf.api.goods.model.dto.CalculateOrderTotalAmountDTO;
import com.lanf.goods.model.entity.GoodsSkuDO;
import com.lanf.api.goods.model.vo.CalculateOrderTotalAmountVO;
import com.lanf.goods.service.goods.IGoodsSkuService;
import com.lanf.goods.utils.GoodsServiceUtils;
import com.lanf.welfare.api.WelfareApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-11
 */
@Service
public class GoodsSkuServiceImpl extends ServiceImpl<GoodsSkuMapper, GoodsSkuDO> implements IGoodsSkuService {

    @Autowired
    private WelfareApiService welfareApiService;




    @Override
    public CalculateOrderTotalAmountVO calculateOrderTotalAmount(CalculateOrderTotalAmountDTO dto) {

        Long skuId = dto.getSkuId();
        GoodsSkuDO goodsSkuDO = this.getById(skuId);
        BigDecimal price = goodsSkuDO.getPrice();
        Integer quantity = dto.getQuantity();
        //订单总金额
        BigDecimal totalAmount =  GoodsServiceUtils.calculateTotalAmount(price, quantity);
        CalculateOrderTotalAmountVO vo = new CalculateOrderTotalAmountVO();
        vo.setTotalAmount(totalAmount);


        return vo;
    }
}
