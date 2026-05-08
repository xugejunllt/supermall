package com.lanf.pay.service.trade;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.client.pay.model.dto.CancelTradeOrderDTO;
import com.lanf.client.pay.model.dto.CreatePayOrderDTO;
import com.lanf.client.pay.model.dto.CreateTradeOrderDTO;
import com.lanf.client.pay.model.dto.TradeOrderQuantitySumDTO;
import com.lanf.client.pay.model.query.TradeOrderBathQuery;
import com.lanf.client.pay.model.query.TradeOrderQuery;
import com.lanf.client.pay.model.vo.*;
import com.lanf.pay.model.dto.BathCreatePrepayOrderDTO;
import com.lanf.pay.model.dto.CreatePrepayOrderDTO;
import com.lanf.pay.model.dto.RechargeDTO;
import com.lanf.pay.model.entity.TradeOrderDO;
import com.lanf.pay.model.vo.CreatePrepayOrderVO;
import com.lanf.pay.model.vo.CreateRechargeTradeOrderVO;
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
     * 创建下单交易订单
     *
     */
    void createTradeOrder(CreateTradeOrderDTO dto);
    /**
     * 确认创建下单交易订单
     *
     */
    void confirmCreateTradeOrder(CreateTradeOrderDTO dto);
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
     * 取消交易单
     *
     */

    CancelTradeOrderVO cancelTradeOrder(CancelTradeOrderDTO dto);

    /**
     * 创建钱包充值交易单
     *
     *
     */
    CreateRechargeTradeOrderVO createRechargeTradeOrder(RechargeDTO dto);


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
