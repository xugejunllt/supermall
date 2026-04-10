package com.lanf.cache.constant;

/**
 * 缓存的key 常量
 *
 * @author ruoyi
 */
public class RedisCacheConstants {

    /**
     * 注册短信验证码
     */
    public static final String REGISTER_CODE_KEY = "register:code:%s"; //
    /**
     * 登入短信验证码
     */
    public static final String LOGIN_CODE_KEY = "login:code:%s"; //

    public static final String USER_TOKEN = "user:token:%s:%s"; //
    public static final String USER_REFRESH_TOKEN = "user:refreshToken:%s:%s"; //


    /**
     * 用户登入失败次数统计
     */
    public static final String LOGIN_FAIL_COUNT = "user:loginFail:%s"; //
    /**
     * 用户地址缓存
     */
    public static final String ADDRESS = "user:address:%s"; //
    /**
     * 商品详情缓存
     */
    public static final String GOODS_DETAIL = "goods:detail:%s"; //
    /**
     * 店铺优惠券模板缓存
     */
    public static final String SHOP_COUPON = "coupon:list:%s"; //
    /**
     * 优惠券模板剩余数量缓存
     */
    public static final String SHOP_COUPON_COUNT = "coupon:count:%s";
    /**
     * 优惠卷模板作废
     *
     */
    public static final String COUPON_REVOKE = "coupon:revoke"; //


    /**
     * token过期时间 2个小时
     */
    public static final long TOKEN_EXP_TIME = 7200L;
    
    /**
     * 支付补偿订单重试策略缓存
     */
    public static final String PAY_RETRY_POLICY = "pay:retry:policy"; // %s 替换为 retryLevel
    
    /**
     * 支付补偿订单重试策略缓存过期时间（分钟）7天
     */
    public static final long PAY_RETRY_POLICY_EXP_TIME = 7*24*60;
}
