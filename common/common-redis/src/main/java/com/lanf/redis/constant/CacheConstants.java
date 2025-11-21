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
    /**
     * 登录用户 redis key
     */
    public static final String LOGIN_TOKEN_KEY = "login_tokens:";




    /**
     * 平台费率配置
     */
    public static final String RATE_CONFIG = "rate_config";
    /**
     * 平台商户id
     */
    public static final String PLATFORM_BUS_ID = "platform_bus_id";


}
