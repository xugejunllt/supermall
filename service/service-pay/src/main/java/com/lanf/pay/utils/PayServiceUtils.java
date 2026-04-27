package com.lanf.pay.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PayServiceUtils {

    /**
     *
     * 生成交易订单号（格式：年月日时分秒毫秒 + 随机数）
     *
     */
    public static String generateOutTradeNo(String orderNumber) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        String timeStr = sdf.format(new Date());

        return  timeStr + orderNumber;
    }
}
