package com.lanf.pay.service;

import com.lanf.common.utils.BeanUtil;
import com.lanf.pay.service.impl.AliPayServiceImpl;
import com.lanf.pay.service.impl.WeiXinPayServiceImpl;

public class PayFactory {

    public static PayService getPayService(Integer payType) {

        if (payType == 0) {
            return BeanUtil.getBean(AliPayServiceImpl.class);
        }
        if (payType == 1) {
            return BeanUtil.getBean(WeiXinPayServiceImpl.class);
        }
        return null;
    }
}
