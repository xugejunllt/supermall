package com.lanf.pay.service.trade;

import com.lanf.api.pay.model.enums.TradePurposeEnum;
import com.lanf.common.utils.BeanUtil;
import com.lanf.constant.exception.BizException;
import com.lanf.pay.service.trade.impl.RealtimeOrderTradesSuccessHandler;
import com.lanf.pay.service.trade.impl.WalletRechargeTradesSuccessHandler;

public class TradeSuccessHandlerFactory {


    public static TradeSuccessHandler getTradeSuccessHandler(TradePurposeEnum tradeType) {

        if (TradePurposeEnum.REALTIME_ORDER.equals(tradeType)) {

            return BeanUtil.getBean(RealtimeOrderTradesSuccessHandler.class);
        }
        if (TradePurposeEnum.WALLET_RECHARGE.equals(tradeType)) {

            return BeanUtil.getBean(WalletRechargeTradesSuccessHandler.class);
        }
        throw new BizException("不支持的交易用途");
    }

}
