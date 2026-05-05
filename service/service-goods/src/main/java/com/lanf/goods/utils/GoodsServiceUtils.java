package com.lanf.goods.utils;

import com.lanf.common.utils.BeanUtil;
import com.lanf.common.utils.BigDecimalUtil;
import com.lanf.constant.exception.BizException;
import com.lanf.goods.model.entity.StockDO;
import com.lanf.goods.service.stock.IStockService;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;
@Slf4j
public class GoodsServiceUtils {


    /**
     * 计算订单总金额
     *
     * @param price 单价
     * @param quantity 数量
     * @return 总金额
     */
    public static BigDecimal calculateTotalAmount(BigDecimal price, Integer quantity) {
        return BigDecimalUtil.multiply(price, BigDecimal.valueOf(quantity));
    }

    public static  StockDO findStockDO(String  skuCode){

        IStockService stockService = BeanUtil.getBean(IStockService.class);
        List<StockDO> stockDOList = stockService.lambdaQuery().eq(StockDO::getSkuCode, skuCode).list();
        if (stockDOList.isEmpty()) {
            log.info("库存不存在");
            throw new BizException("库存不存在");
        }
        /**
         *
         * 可能多个仓库 skucode 暂时取其中一个
         *
         */

        return stockDOList.get(0);
    }
}
