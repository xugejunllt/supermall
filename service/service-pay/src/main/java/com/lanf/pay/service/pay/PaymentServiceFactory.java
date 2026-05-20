package com.lanf.pay.service.pay;

import com.lanf.api.pay.model.enums.PayChannelEnum;
import com.lanf.constant.exception.BizException;
import com.lanf.pay.service.pay.impl.AliPayPaymentServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class PaymentServiceFactory {

    @Autowired
    private AliPayPaymentServiceImpl aliPayPaymentService;

    private static final Map<Integer, PaymentService> PAYMENT_SERVICE_MAP = new HashMap<>();

    @PostConstruct
    public void init() {
        PAYMENT_SERVICE_MAP.put(PayChannelEnum.ALI_PAY.getCode(), aliPayPaymentService);
        log.info("支付服务工厂初始化完成，支持的支付渠道: {}", PAYMENT_SERVICE_MAP.keySet());
    }

    public PaymentService getPaymentService(Integer type) {
        PaymentService paymentService = PAYMENT_SERVICE_MAP.get(type);
        
        if (paymentService != null) {
            return paymentService;
        }

        log.error("不支持的支付类型[{}]", type);
        throw new BizException("不支持的支付类型");
    }
}
