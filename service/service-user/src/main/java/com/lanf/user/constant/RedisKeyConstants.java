package com.lanf.user.constant;

public class RedisKeyConstants {

    public static final String USER_ACCESS_TOKEN = "user:access:token:%s:%s";

    public static final String USER_REFRESH_TOKEN = "user:refresh:token:%s:%s";

    public static final long ACCESS_TOKEN_DEFAULT_EXP_DAYS = 7L;

    public static final long REFRESH_TOKEN_DEFAULT_EXP_DAYS = 30L;
}
