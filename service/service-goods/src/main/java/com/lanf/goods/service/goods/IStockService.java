package com.lanf.goods.service.goods;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.goods.model.bo.SkuCodeStockBO;
import com.lanf.goods.model.dto.DeductStockDTO;
import com.lanf.goods.model.dto.StockEnoughDTO;
import com.lanf.goods.model.entity.StockDO;
import com.lanf.goods.model.vo.DeductStockVO;
import com.lanf.goods.model.vo.StockEnoughVO;
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

    /**
     * 扣减库存
     *
     */
    DeductStockVO deductStock(DeductStockDTO deductStockDTO);

    /**
     *  判断库存是否充足
     *
     *
     */
    StockEnoughVO isStockEnough(StockEnoughDTO dto);


}
