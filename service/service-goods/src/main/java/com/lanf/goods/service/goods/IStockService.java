package com.lanf.goods.service.goods;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.goods.model.bo.SkuCodeStockBO;
import com.lanf.goods.model.entity.StockDO;
import com.lanf.rocketmq.model.message.UserStockAddMsg;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 库存 服务类
 * </p>
 *
 * @author jarven
 * @since 2025-11-29
 */
public interface IStockService extends IService<StockDO> {


    void addUserStock(UserStockAddMsg message);

    //key:skuCode
    Map<String,SkuCodeStockBO> findBySkuCode(List<String> skuCode);

}
