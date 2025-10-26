package com.lanf.common.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
 
public class BigDecimalUtils {
 
    private static final int DEFAULT_SCALE = 2; // 默认保留小数点后两位
    private static final RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.HALF_UP; // 默认四舍五入方式
 
    /**
     * 加法运算
     * @param v1 被加数
     * @param v2 加数
     * @return 结果
     */
    public static BigDecimal add(BigDecimal v1, BigDecimal v2) {
        return v1.add(v2).setScale(DEFAULT_SCALE, DEFAULT_ROUNDING_MODE);
    }
 
    /**
     * 减法运算
     * @param v1 被减数
     * @param v2 减数
     * @return 结果
     */
    public static BigDecimal subtract(BigDecimal v1, BigDecimal v2) {
        if (v2 == null){
           v2 = new BigDecimal(0);
        }
        return v1.subtract(v2).setScale( DEFAULT_SCALE, DEFAULT_ROUNDING_MODE);
    }
 
    /**
     * 乘法运算
     * @param v1 被乘数
     * @param v2 乘数
     * @return 结果
     */
    public static BigDecimal multiply(BigDecimal v1, BigDecimal v2) {
        return v1.multiply(v2).setScale( DEFAULT_SCALE, DEFAULT_ROUNDING_MODE);
    }
 
    /**
     * 除法运算
     * @param v1 被除数
     * @param v2 除数
     * @return 结果
     */
    public static BigDecimal divide(BigDecimal v1, BigDecimal v2) {
        return v1.divide(v2, DEFAULT_SCALE, DEFAULT_ROUNDING_MODE);
    }
 
    /**
     * 比较大小
     * @param v1 第一个数
     * @param v2 第二个数
     * @return 如果v1大于v2返回1，小于返回-1，等于返回0
     */
    public static int compareTo(BigDecimal v1, BigDecimal v2) {
        return v1.setScale(DEFAULT_SCALE, DEFAULT_ROUNDING_MODE).compareTo(v2.setScale(DEFAULT_SCALE, DEFAULT_ROUNDING_MODE));
    }
 
    /**
     * 提供默认值，避免空指针异常
     * @param value 可能为null的BigDecimal对象
     * @return 如果为null则返回0，否则返回原对象
     */
    public static BigDecimal defaultValue(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
 
    /**
     * 格式化输出，保留指定小数位
     * @param bd BigDecimal对象
     * @param scale 保留的小数位数
     * @return 格式化后的字符串
     */
    public static String format(BigDecimal bd, int scale) {
        return bd.setScale(scale, DEFAULT_ROUNDING_MODE).toPlainString();
    }
    public static BigDecimal scale(BigDecimal bd) {
        return bd.setScale(DEFAULT_SCALE, DEFAULT_ROUNDING_MODE);
    }
    public static String format(BigDecimal bd) {
        return bd.setScale(DEFAULT_SCALE, DEFAULT_ROUNDING_MODE).toPlainString();
    }
}