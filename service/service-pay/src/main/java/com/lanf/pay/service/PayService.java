package com.lanf.pay.service;

import com.lanf.pay.model.bo.RefundBO;
import com.lanf.pay.model.bo.TradeStatusBO;
import com.lanf.pay.model.dto.*;
import com.lanf.pay.model.vo.*;

import java.util.List;

/**
 * 支付接口
 */
public interface PayService {

    /**
     * 支付成功回调
     *
     *
     */
    void payCallback(PayCallbackDTO dto);

    /**
     * 手动补单 对于超时支付成功没有收到通知的订单
     *
     */
    void patChPayOrder(Long orderId);
    /**
     * 转账
     *
     *
     */
    TransferAccountsVO transferAccounts(TransferAccountsDTO dto);



    /**
     * 生成批量预支付信息
     *
     *
     */
    TradeOrderVO bathPay(BathPayDTO dto);
    /**
     * 生成单笔预支付信息
     *
     *
     */
    TradeOrderVO onePay(OnePayDTO dto);
    /**
     *
     * 查询交易单状态
     *
     */
    TradeStatusBO queryTradeStatus(Long orderId);

    /**
     * 取消交易单
     *
     */
    void cancelTradeOrder(Long orderId);

    /**
     * 退款
     *
     */
    void refund(RefundBO refundBO);
}
