package com.lanf.goods.service.stock;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.api.goods.model.dto.BathDeductStockDTO;
import com.lanf.api.goods.model.dto.DeductStockDTO;
import com.lanf.api.goods.model.dto.SeckillStockPreoccupationDTO;
import com.lanf.api.goods.model.query.UserStockPageQuery;
import com.lanf.api.goods.model.vo.DeductStockVO;
import com.lanf.api.goods.model.vo.StockPageVO;
import com.lanf.api.storage.mq.message.PublishStockMessage;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.goods.model.bo.RollbackStockBO;
import com.lanf.goods.model.dto.StockEnoughDTO;
import com.lanf.goods.model.dto.SubmitCartStockEnoughDTO;
import com.lanf.goods.model.entity.StockDO;
import com.lanf.goods.model.query.StockQueryByGoodsIdQuery;
import com.lanf.goods.model.vo.StockEnoughVO;
import com.lanf.goods.model.vo.StockWithDistanceVO;
import com.lanf.seckill.mq.message.SecKillPlaneMessage;

import java.util.List;

/**
 * <p>
 * 库存 服务类
 * </p>
 *
 * @author jarven
 * @since 2025-11-29
 */
public interface IStockService extends IService<StockDO> {




    /**
     * 扣减库存
     *
     */
    DeductStockVO deductStock(DeductStockDTO deductStockDTO);

    DeductStockVO deductStock(DeductStockDTO deductStockDTO, boolean queryResult);

     void confirmDeductStock(DeductStockDTO deductStockDTO);

     void cancelDeductStock(DeductStockDTO deductStockDTO);
    /**
     * 批量扣减库存
     *
     */
    void bathDeductStock(BathDeductStockDTO deductStockDTO);

     void confirmBathDeductStock(BathDeductStockDTO deductStockDTO);

    void cancelBathDeductStock(BathDeductStockDTO deductStockDTO);
    /**
     *  判断库存是否充足
     *
     *
     */
    StockEnoughVO isStockEnough(StockEnoughDTO dto);

    /**
     * 提交购物车时 检查库存是否足够
     *
     */
    List<StockEnoughVO> submitCartStockEnough(SubmitCartStockEnoughDTO dto);

    /**
     * 添加秒杀商品预占库存
     *
     */
    void seckillStockPreoccupation(SeckillStockPreoccupationDTO dto);

    /**
     * 分页查询库存
     */
    PageResult<StockPageVO> stockPageQuery(UserStockPageQuery query);

    /**
     * 根据 SKU 编码列表查询库存（带距离计算）
     * @param dto 查询参数
     * @return 库存信息列表
     */
    List<StockWithDistanceVO> stockQueryByGoodsId(StockQueryByGoodsIdQuery dto);

    /**
     * 取消订单时 回滚库存
     *

     */
    void rollbackStock(RollbackStockBO rollbackStock);
    void publishStock(PublishStockMessage message);

    void secKillPlane(SecKillPlaneMessage message);

}
