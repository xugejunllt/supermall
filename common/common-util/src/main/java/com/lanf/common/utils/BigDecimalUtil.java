package com.lanf.common.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Objects;

/**
 * BigDecimal 工具类
 * 提供精确的数值计算、格式化、比较等功能
 */
public class BigDecimalUtil {

    // 默认除法运算精度（保留小数位数）
    private static final int DEFAULT_DIV_SCALE = 2;
    
    // 默认舍入模式 四舍五入
    private static final RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.HALF_UP;

    /**
     * 私有构造方法，防止实例化
     */
    private BigDecimalUtil() {
        throw new AssertionError("不允许实例化工具类");
    }

    // ==================== 创建方法 ====================

    /**
     * 安全创建 BigDecimal（处理 null 值）
     */
    public static BigDecimal safeValue(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    public static BigDecimal safeValue(BigDecimal value, BigDecimal defaultValue) {
        return value == null ? defaultValue : value;
    }

    public static BigDecimal valueOf(String value) {
        return isBlank(value) ? BigDecimal.ZERO : new BigDecimal(value.trim());
    }

    public static BigDecimal valueOf(Double value) {
        return value == null ? BigDecimal.ZERO : BigDecimal.valueOf(value);
    }

    public static BigDecimal valueOf(Integer value) {
        return value == null ? BigDecimal.ZERO : new BigDecimal(value);
    }

    public static BigDecimal valueOf(Long value) {
        return value == null ? BigDecimal.ZERO : new BigDecimal(value);
    }

    // ==================== 基本运算 ====================

    /**
     * 加法运算
     */
    public static BigDecimal add(BigDecimal a, BigDecimal b) {

        return safeValue(a).add(safeValue(b)).setScale(DEFAULT_DIV_SCALE, DEFAULT_ROUNDING_MODE);
    }

    /**
     * 加法运算（多个值）
     */
    public static BigDecimal add(BigDecimal... values) {
        if (values == null || values.length == 0) {
            return BigDecimal.ZERO;
        }
        return Arrays.stream(values)
                .map(BigDecimalUtil::safeValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(DEFAULT_DIV_SCALE, DEFAULT_ROUNDING_MODE);
    }

    /**
     * 减法运算
     */
    public static BigDecimal subtract(BigDecimal a, BigDecimal b) {
        return safeValue(a).subtract(safeValue(b));
    }

    /**
     * 乘法运算
     */
    public static BigDecimal multiply(BigDecimal a, BigDecimal b) {
        return safeValue(a).multiply(safeValue(b));
    }

    /**
     * 乘法运算（保留指定位数小数）
     */
    public static BigDecimal multiply(BigDecimal a, BigDecimal b, int scale) {
        return multiply(a, b, scale, DEFAULT_ROUNDING_MODE);
    }

    public static BigDecimal multiply(BigDecimal a, BigDecimal b, int scale, RoundingMode roundingMode) {
        return multiply(a, b).setScale(scale, roundingMode);
    }

    /**
     * 除法运算
     */
    public static BigDecimal divide(BigDecimal dividend, BigDecimal divisor) {
        return divide(dividend, divisor, DEFAULT_DIV_SCALE, DEFAULT_ROUNDING_MODE);
    }

    public static BigDecimal divide(BigDecimal dividend, BigDecimal divisor, int scale) {
        return divide(dividend, divisor, scale, DEFAULT_ROUNDING_MODE);
    }

    public static BigDecimal divide(BigDecimal dividend, BigDecimal divisor, int scale, RoundingMode roundingMode) {
        if (divisor == null || divisor.compareTo(BigDecimal.ZERO) == 0) {
            throw new ArithmeticException("除数不能为零");
        }
        return safeValue(dividend).divide(safeValue(divisor), scale, roundingMode);
    }

    // ==================== 比较运算 ====================

    /**
     * 判断是否相等（忽略精度）
     */
    public static boolean equals(BigDecimal a, BigDecimal b) {
        return safeValue(a).compareTo(safeValue(b)) == 0;
    }

    /**
     * 判断是否相等（指定精度）
     */
    public static boolean equals(BigDecimal a, BigDecimal b, int scale) {
        return safeValue(a).setScale(scale, DEFAULT_ROUNDING_MODE)
                .compareTo(safeValue(b).setScale(scale, DEFAULT_ROUNDING_MODE)) == 0;
    }

    /**
     * 判断是否大于
     */
    public static boolean gt(BigDecimal a, BigDecimal b) {
        return safeValue(a).compareTo(safeValue(b)) > 0;
    }

    /**
     * 判断是否大于等于
     */
    public static boolean ge(BigDecimal a, BigDecimal b) {
        return safeValue(a).compareTo(safeValue(b)) >= 0;
    }

    /**
     * 判断是否小于
     */
    public static boolean lt(BigDecimal a, BigDecimal b) {
        return safeValue(a).compareTo(safeValue(b)) < 0;
    }

    /**
     * 判断是否小于等于
     */
    public static boolean le(BigDecimal a, BigDecimal b) {
        return safeValue(a).compareTo(safeValue(b)) <= 0;
    }

    /**
     * 判断是否为负数
     */
    public static boolean isNegative(BigDecimal value) {
        return safeValue(value).compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * 判断是否为正数
     */
    public static boolean isPositive(BigDecimal value) {
        return safeValue(value).compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 判断是否为零
     */
    public static boolean isZero(BigDecimal value) {
        return safeValue(value).compareTo(BigDecimal.ZERO) == 0;
    }

    // ==================== 格式化与转换 ====================

    /**
     * 格式化金额（千分位，保留两位小数）
     */
    public static String formatCurrency(BigDecimal value) {
        return format(value, "#,##0.00");
    }

    /**
     * 格式化百分比（保留两位小数）
     */
    public static String formatPercent(BigDecimal value) {
        BigDecimal percent = multiply(value, BigDecimal.valueOf(100), 2);
        return percent.stripTrailingZeros().toPlainString() + "%";
    }

    /**
     * 自定义格式化
     */
    public static String format(BigDecimal value, String pattern) {
        if (value == null) return "";
        DecimalFormat df = new DecimalFormat(pattern);
        return df.format(value);
    }

    /**
     * 转换为字符串（去除末尾多余的零）
     */
    public static String toPlainString(BigDecimal value) {
        return safeValue(value).stripTrailingZeros().toPlainString();
    }

    /**
     * 转换为分（金额元转分）
     */
    public static long toFen(BigDecimal yuan) {
        return multiply(yuan, BigDecimal.valueOf(100)).longValue();
    }

    /**
     * 转换为元（金额分转元）
     */
    public static BigDecimal toYuan(long fen) {
        return divide(BigDecimal.valueOf(fen), BigDecimal.valueOf(100), 2);
    }

    // ==================== 数值处理 ====================

    /**
     * 保留指定位数小数
     */
    public static BigDecimal scale(BigDecimal value, int scale) {
        return scale(value, scale, DEFAULT_ROUNDING_MODE);
    }

    public static BigDecimal scale(BigDecimal value, int scale, RoundingMode roundingMode) {
        return safeValue(value).setScale(scale, roundingMode);
    }

    /**
     * 获取最大值
     */
    public static BigDecimal max(BigDecimal... values) {
        if (values == null || values.length == 0) {
            return BigDecimal.ZERO;
        }
        return Arrays.stream(values)
                .filter(Objects::nonNull)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    /**
     * 获取最小值
     */
    public static BigDecimal min(BigDecimal... values) {
        if (values == null || values.length == 0) {
            return BigDecimal.ZERO;
        }
        return Arrays.stream(values)
                .filter(Objects::nonNull)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    /**
     * 绝对值
     */
    public static BigDecimal abs(BigDecimal value) {
        return safeValue(value).abs();
    }

    /**
     * 取相反数
     */
    public static BigDecimal negate(BigDecimal value) {
        return safeValue(value).negate();
    }

    // ==================== 验证与校验 ====================

    /**
     * 验证是否为有效的数值
     */
    public static boolean isValid(String value) {
        if (isBlank(value)) {
            return false;
        }
        try {
            new BigDecimal(value.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 验证是否在范围内（包含边界）
     */
    public static boolean isInRange(BigDecimal value, BigDecimal min, BigDecimal max) {
        BigDecimal v = safeValue(value);
        return ge(v, min) && le(v, max);
    }

    /**
     * 验证是否在范围内（不包含边界）
     */
    public static boolean isBetween(BigDecimal value, BigDecimal min, BigDecimal max) {
        BigDecimal v = safeValue(value);
        return gt(v, min) && lt(v, max);
    }

    /**
     * 检查是否为 null 或零
     */
    public static boolean isNullOrZero(BigDecimal value) {
        return value == null || value.compareTo(BigDecimal.ZERO) == 0;
    }

    // ==================== 业务常用方法 ====================

    /**
     * 计算百分比
     */
    public static BigDecimal calculatePercentage(BigDecimal part, BigDecimal total) {
        if (isNullOrZero(total)) {
            return BigDecimal.ZERO;
        }
        return divide(part, total, 4).multiply(BigDecimal.valueOf(100));
    }

    /**
     * 计算增长率
     */
    public static BigDecimal calculateGrowthRate(BigDecimal current, BigDecimal previous) {
        if (isNullOrZero(previous)) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? BigDecimal.valueOf(100) : BigDecimal.ZERO;
        }
        return divide(subtract(current, previous), previous, 4)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * 计算平均值
     */
    public static BigDecimal average(BigDecimal... values) {
        if (values == null || values.length == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal sum = add(values);
        return divide(sum, BigDecimal.valueOf(values.length), 2);
    }

    /**
     * 计算折扣价
     */
    public static BigDecimal calculateDiscount(BigDecimal originalPrice, BigDecimal discountRate) {
        return multiply(originalPrice, discountRate.divide(BigDecimal.valueOf(100), 4, DEFAULT_ROUNDING_MODE), 2);
    }

    // ==================== 辅助方法 ====================

    private static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    // ==================== 常量定义 ====================

    public static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    public static final BigDecimal ONE_THOUSAND = new BigDecimal("1000");
    public static final BigDecimal ONE_MILLION = new BigDecimal("1000000");
    
    /**
     * 常用舍入模式快捷方法
     */
    public static class Rounding {
        public static final RoundingMode UP = RoundingMode.UP;
        public static final RoundingMode DOWN = RoundingMode.DOWN;
        public static final RoundingMode HALF_UP = RoundingMode.HALF_UP;
        public static final RoundingMode HALF_DOWN = RoundingMode.HALF_DOWN;
        public static final RoundingMode HALF_EVEN = RoundingMode.HALF_EVEN;
        public static final RoundingMode CEILING = RoundingMode.CEILING;
        public static final RoundingMode FLOOR = RoundingMode.FLOOR;
    }
}