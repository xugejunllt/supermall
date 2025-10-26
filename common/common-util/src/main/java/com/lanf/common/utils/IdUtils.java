package com.lanf.common.utils;


/**
 * ID生成器工具类
 *
 * @author ruoyi
 */
public class IdUtils {

    private static final SnowflakeIdWorker snowflakeIdWorker = new SnowflakeIdWorker(1L, 1L);


    /**
     * 简化的UUID，去掉了横线，使用性能更好的ThreadLocalRandom生成UUID
     *
     * @return 简化的UUID，去掉了横线
     */
    public static String fastSimpleUUID() {
        return UUID.fastUUID().toString(true);
    }

    public static long generateId() {

        return snowflakeIdWorker.nextId();
    }

}
