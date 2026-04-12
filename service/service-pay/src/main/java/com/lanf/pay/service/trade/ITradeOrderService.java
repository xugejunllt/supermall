package com.lanf.pay.service.trade;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.pay.model.bo.PaySuccessHandleBO;
import com.lanf.pay.model.bo.PaySuccessHandleResultBO;
import com.lanf.pay.model.dto.*;
import com.lanf.pay.model.entity.TradeOrderDO;
import com.lanf.pay.model.query.TradeOrderBathQuery;
import com.lanf.pay.model.query.TradeOrderQuery;
import com.lanf.pay.model.vo.*;
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

    /**
     * 创建预支付信息
     *
     *
     */
    CreatePrepayOrderVO createPrepayOrder(CreatePrepayOrderDTO dto);
    /**
     * 批量创建预支付信息
     *
     */
    CreatePrepayOrderVO bathCreatePrepayOrder(BathCreatePrepayOrderDTO dto);
    /**
     * 支付成功回调通知
     *
     */
    void payCallback(PayCallbackDTO dto);

    /**
     * 支付成功处理
     *
     *
     */
     PaySuccessHandleResultBO paySuccessHandleBO(PaySuccessHandleBO paySuccessHandleBO);

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
