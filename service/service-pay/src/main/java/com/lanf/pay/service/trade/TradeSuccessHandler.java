package com.lanf.pay.service.trade;


import com.lanf.pay.model.bo.PostTradeSuccessHandlerContext;

/**
 * 交易成功处理器抽象接口
 * 基于不同的交易用途 -> TradePurposeEnum
 *
 */
public interface TradeSuccessHandler {


    void  postTradeSuccessHandler(PostTradeSuccessHandlerContext  context);
}
