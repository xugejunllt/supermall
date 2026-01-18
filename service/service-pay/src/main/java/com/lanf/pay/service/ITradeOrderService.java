package com.lanf.pay.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.pay.model.dto.CreatePayOrderDTO;
import com.lanf.pay.model.dto.CreateTradeOrderDTO;
import com.lanf.pay.model.dto.TradeOrderQuantitySumDTO;
import com.lanf.pay.model.entity.TradeOrderDO;
import com.lanf.pay.model.query.TradeOrderBathQuery;
import com.lanf.pay.model.query.TradeOrderQuery;
import com.lanf.pay.model.vo.CreatePayOrderVO;
import com.lanf.pay.model.vo.OrderTradeVO;
import com.lanf.pay.model.vo.TradeOrderApiVO;
import com.lanf.pay.model.vo.TradeOrderBathVO;
import com.lanf.rocketmq.model.message.RefundDTO;

import java.util.List;

/**
 * <p>
 * 交易订单 服务类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-14
 */
public interface ITradeOrderService extends IService<TradeOrderDO> {


    /**
     * 创建交易订单
     *
     */
    void createTradeOrder(CreateTradeOrderDTO dto);




    @Deprecated
    CreatePayOrderVO createPayOrder(List<CreatePayOrderDTO> dto);
    @Deprecated
    OrderTradeVO queryOrderTradeByOrderId(Long orderId);

    /**
     * 进行退款
     *
     */
    @Deprecated
    void  refund(RefundDTO dto);

    @Deprecated
    TradeOrderApiVO tradeOrderQuery(TradeOrderQuery query);

    /**
     *
     *
     * 统计交易单数量
     *
     */
    @Deprecated
    Integer tradeOrderQuantitySum(TradeOrderQuantitySumDTO dto);

    /**
     * 查询交易单信息
     *
     *
     */
    @Deprecated
    List<TradeOrderBathVO> tradeOrderBathQuery(TradeOrderBathQuery query);

}
