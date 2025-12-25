package com.lanf.redis.constant;

/**
 * 缓存的key 常量
 *
 * @author ruoyi
 */
public class CacheConstants {

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
    //public static final int TOKEN_EXP_TIME = 2 * 60;
    public static final int TOKEN_EXP_TIME = 5 * 24 * 60;
    /**
     * 刷新token过期时间 7天
     */
    public static final int REFRESH_TOKEN_EXP_TIME = 7 * 24 * 60;

    public static final int ADMIN_TOKEN_EXP_TIME = 5 * 24 * 60;
    public static final int ADMIN_REFRESH_TOKEN_EXP_TIME = 7 * 24 * 60;
    /**
     * 缓存时间7天
     */
    public static final int GOODS_DETAIL_EXP_TIME = 7 * 24 * 60;
    /**
     * 缓存时间7天
     */
    public static final int SHOP_COUPON_EXP_TIME = 7 * 24 * 60;
    public static final long SHOP_COUPON_COUNT_EXP_TIME = 7 * 24 * 60;
    public static final int COUPON_REVOKE_EXP_TIME = 7 * 24 * 60;

    /**
     * 缓存时间1天
     */
    public static final int LOGIN_FAIL_COUNT_TIME = 24 * 60;

    public static final int ADDRESS_TIME = 3*24 * 60;

    /**
     *
     */
    public static final String ADMIN_USER_TOKEN = "admin:token:%s:%s"; //
    public static final String ADMIN_USER_REFRESH_TOKEN = "admin:refreshToken:%s:%s"; //
    public static final String ADMIN_AUTH = "admin:auth:%s:%s"; //
    public static final int ADMIN_ADMIN_AUTH_EXP_TIME = ADMIN_REFRESH_TOKEN_EXP_TIME;

    public static final String ADMIN_USER_INFO = "admin:info:%s"; //

    public static final int ADMIN_USER_INFO_EXP_TIME = ADMIN_REFRESH_TOKEN_EXP_TIME;


    /**
     * 获取key方法
     */
    public static String getUSER_TOKEN_KEY(Integer channel, Long userId) {

        return String.format(CacheConstants.USER_TOKEN, channel, userId);
    }

    public static String getUSER_REFRESH_TOKEN(Integer channel, Long userId) {

        return String.format(CacheConstants.USER_REFRESH_TOKEN, channel, userId);
    }
    public static String getLOGIN_FAIL_COUNT( String phoneNumber) {

        return String.format(CacheConstants.LOGIN_FAIL_COUNT,phoneNumber);
    }
    public static String getADDRESS( Long userId) {

        return String.format(CacheConstants.ADDRESS, userId);
    }
    public static String getADMIN_USER_TOKEN(Integer channel, Long userId) {

        return String.format(CacheConstants.ADMIN_USER_TOKEN, channel, userId);
    }
    public static String getADMIN_USER_REFRESH_TOKEN(Integer channel, Long userId) {

        return String.format(CacheConstants.ADMIN_USER_REFRESH_TOKEN, channel, userId);
    }
    public static String getADMIN_AUTH(Integer channel, Long userId) {

        return String.format(CacheConstants.ADMIN_AUTH, channel, userId);
    }
    public static String getADMIN_INFO( Long userId) {

        return String.format(CacheConstants.ADMIN_USER_INFO, userId);
    }

    public static String getGOODS_DETAIL( Long goodsId) {

        return String.format(CacheConstants.GOODS_DETAIL, goodsId);
    }
    public static String getSHOP_COUPON( Long shopId) {

        return String.format(CacheConstants.SHOP_COUPON, shopId);
    }

    public static String getSHOP_COUPON_COUNT(Long shopId) {

        return String.format(CacheConstants.SHOP_COUPON_COUNT, shopId);
    }

    public static String getCOUPON_REVOKE() {

        return String.format(CacheConstants.COUPON_REVOKE);
    }



    /**
     * 平台费率配置
     */
    public static final String RATE_CONFIG = "rate_config";
    /**
     * 平台商户id
     */
    public static final String PLATFORM_BUS_ID = "platform_bus_id";


}
