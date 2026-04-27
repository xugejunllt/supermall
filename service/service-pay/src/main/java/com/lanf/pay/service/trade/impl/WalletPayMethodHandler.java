package com.lanf.pay.service.trade.impl;

import com.lanf.pay.model.bo.CancelPayOrderContext;
import com.lanf.pay.service.trade.PayMethodHandler;
import org.springframework.stereotype.Service;

@Service
public class WalletPayMethodHandler implements PayMethodHandler {


    @Override
    public void cancelPayOrder(CancelPayOrderContext context) {

    }
}
