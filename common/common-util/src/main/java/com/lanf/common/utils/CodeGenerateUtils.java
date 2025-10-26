package com.lanf.common.utils;

import java.util.Random;

public class CodeGenerateUtils {

    /**
     * 生成20位纯大写随机字母 租户编码
     * @return
     */
    public static  String generaCode(){

        StringBuffer stringBuffer = new StringBuffer();

        for (int i=0;i<19;i++) {
            Random random = new Random();
            char letter = (char) (random.nextInt(26) + 'A');
            stringBuffer.append(letter);
        }
        return stringBuffer.toString();
    }

    /**
     * 生成订单编号
     *
     */
    public static  String generateOrderNumber(){


        return IdUtils.generateId()+"";
    }

}
