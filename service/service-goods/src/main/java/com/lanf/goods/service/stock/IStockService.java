package com.lanf.goods.service.stock;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.api.goods.model.dto.DeductStockDTO;
import com.lanf.api.goods.model.dto.SeckillStockPreoccupationDTO;
import com.lanf.api.goods.model.query.UserStockPageQuery;
import com.lanf.api.goods.model.vo.DeductStockVO;
import com.lanf.api.goods.model.vo.StockPageVO;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.goods.model.dto.StockEnoughDTO;
import com.lanf.goods.model.entity.StockDO;
import com.lanf.goods.model.vo.StockEnoughVO;

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


    //key:skuCode
    Map<String,StockDO> findBySkuCode(List<String> skuCode);

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

    /**
     * 添加秒杀商品预占库存
     *
     */
    void seckillStockPreoccupation(SeckillStockPreoccupationDTO dto);

    /**
     * 分页查询库存
     */
    PageResult<StockPageVO> stockPageQuery(UserStockPageQuery query);

}
