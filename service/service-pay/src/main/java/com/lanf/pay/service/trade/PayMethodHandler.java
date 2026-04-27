package com.lanf.pay.service.trade;

import com.lanf.pay.model.bo.CancelPayOrderContext;

public interface PayMethodHandler {

    void cancelPayOrder(CancelPayOrderContext  context);
}
