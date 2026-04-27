package com.lanf.pay.service.trade;

import com.lanf.client.pay.model.enums.PayMethodEnum;
import com.lanf.common.utils.BeanUtil;
import com.lanf.constant.exception.BizException;
import com.lanf.pay.service.trade.impl.ThirdPartyPayMethodHandler;
import com.lanf.pay.service.trade.impl.WalletPayMethodHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PayMethodHandlerFactory {

    public static  PayMethodHandler getPayMethodHandler(PayMethodEnum payMethod) {
        if (PayMethodEnum.THIRD_PARTY_PAY.equals(payMethod)){

            return BeanUtil.getBean(ThirdPartyPayMethodHandler.class);

        }
        if (PayMethodEnum.WALLET_BALANCE.equals(payMethod)){

            return BeanUtil.getBean(WalletPayMethodHandler.class);

        }
        log.error("不支持的支付方式");
        throw new BizException("不支持的支付方式");
    }
}
