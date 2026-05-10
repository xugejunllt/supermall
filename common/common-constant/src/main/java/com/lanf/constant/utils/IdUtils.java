package com.lanf.constant.utils;


/**
 * ID生成器工具类
 *
 * @author ruoyi
 */
public class IdUtils {

    private static final SnowflakeIdWorker snowflakeIdWorker = new SnowflakeIdWorker(1L, 1L);




    public static long generateId() {

        return snowflakeIdWorker.nextId();
    }

}
