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

    public static final String USER_SESSION = "user:session:%s:%s"; //


    /**
     * 过期时间
     */
    public static final int USER_SESSION_TIME = 7*24*60; //


    /**
     * 获取key方法
     */
     public static String getUSER_SESSION_KEY(Integer channel,Long userId){

         return String.format(CacheConstants.USER_SESSION, channel, userId);
     }


    /**
     * 登录用户 redis key
     */
    public static final String LOGIN_TOKEN_KEY = "login_tokens:";


    /**
     * 参数管理 cache key
     */
    public static final String SYS_CONFIG_KEY = "sys_config:";

    /**
     * 字典管理 cache key
     */
    public static final String SYS_DICT_KEY = "sys_dict:";

    /**
     * 防重提交 redis key
     */
    public static final String REPEAT_SUBMIT_KEY = "repeat_submit:";

    /**
     * 限流 redis key
     */
    public static final String RATE_LIMIT_KEY = "rate_limit:";

    /**
     * 登录账户密码错误次数 redis key
     */
    public static final String PWD_ERR_CNT_KEY = "pwd_err_cnt:";

    public static final String USER_TOKEN = "user_token:";
    /**
     * 平台费率配置
     */
    public static final String RATE_CONFIG = "rate_config";
    /**
     * 平台商户id
     */
    public static final String PLATFORM_BUS_ID  = "platform_bus_id";








}
