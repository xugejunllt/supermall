package com.lanf.pay.service.pay;

import com.lanf.api.pay.model.enums.PayChannelEnum;
import com.lanf.constant.exception.BizException;
import com.lanf.pay.service.pay.impl.AliPayPaymentServiceImpl;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PaymentServiceFactory {


    public static PaymentService getPaymentService(Integer type) {
        if (PayChannelEnum.ALI_PAY.getCode().equals( type)) {
            return new AliPayPaymentServiceImpl();
        }

        log.error("不支持的支付类型[{}]", type);
      throw new BizException("不支持的支付类型");
    }
}
