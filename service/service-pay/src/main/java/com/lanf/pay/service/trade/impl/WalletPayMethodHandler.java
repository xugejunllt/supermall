package com.lanf.pay.service.trade.impl;

import com.lanf.pay.model.bo.CancelPayOrderContext;
import com.lanf.pay.service.trade.PayMethodHandler;
import com.lanf.pay.service.wallet.IWalletAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WalletPayMethodHandler implements PayMethodHandler {

    @Autowired
    private IWalletAccountService walletAccountService;

    @Override
    public void cancelPayOrder(CancelPayOrderContext context) {
        walletAccountService.rollbackWalletBalanceOnCancelOrder(context.getTradeOrderId());
    }
}
