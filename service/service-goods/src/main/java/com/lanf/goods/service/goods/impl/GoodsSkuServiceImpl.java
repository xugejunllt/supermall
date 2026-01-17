package com.lanf.goods.service.goods.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BigDecimalUtil;
import com.lanf.constant.result.Result;
import com.lanf.constant.result.RpcResultParser;
import com.lanf.goods.mapper.GoodsSkuMapper;
import com.lanf.goods.model.dto.CalculateOrderAmountDTO;
import com.lanf.goods.model.entity.GoodsSkuDO;
import com.lanf.goods.model.vo.CalculateOrderAmountVO;
import com.lanf.goods.service.goods.IGoodsSkuService;
import com.lanf.goods.utils.ProductServiceUtils;
import com.lanf.security.utils.UserIdContext;
import com.lanf.welfare.api.WelfareApiService;
import com.lanf.welfare.model.dto.CalculateDiscountAmountDTO;
import com.lanf.welfare.model.vo.CalculateDiscountAmountVO;
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
    public CalculateOrderAmountVO calculateOrderAmount(CalculateOrderAmountDTO dto) {

        Long skuId = dto.getSkuId();
        Long userId = UserIdContext.getUserId();
        GoodsSkuDO goodsSkuDO = this.getById(skuId);
        BigDecimal price = goodsSkuDO.getPrice();
        Integer quantity = dto.getQuantity();
        //订单总金额
        BigDecimal totalAmount =  ProductServiceUtils.calculateTotalAmount(price, quantity);
        //计算优惠金额
        CalculateDiscountAmountVO amountVO = calculateDiscountAmount(dto, userId, totalAmount);

        /**
         * 构建返回结果
         */
        CalculateOrderAmountVO orderAmountVO = new CalculateOrderAmountVO();
        orderAmountVO.setTotalAmount(totalAmount);
        orderAmountVO.setDiscountAmount(amountVO.getTotalDiscountAmount());
        orderAmountVO.setPayAmount(BigDecimalUtil.subtract(totalAmount, amountVO.getTotalDiscountAmount()));

        return orderAmountVO;
    }

    private CalculateDiscountAmountVO calculateDiscountAmount(CalculateOrderAmountDTO dto, Long userId, BigDecimal totalAmount) {
        CalculateDiscountAmountDTO discountAmountDTO = new CalculateDiscountAmountDTO();
        discountAmountDTO.setUserId(userId);
        discountAmountDTO.setShopId(dto.getSkuId());
        discountAmountDTO.setTotalAmount(totalAmount);

        Result<CalculateDiscountAmountVO> calculateDiscountAmountVOResult = welfareApiService.calculateDiscountAmount(discountAmountDTO);

        return RpcResultParser.parseResult(calculateDiscountAmountVOResult);
    }
}
