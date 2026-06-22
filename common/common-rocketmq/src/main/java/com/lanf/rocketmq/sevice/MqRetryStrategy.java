package com.lanf.rocketmq.sevice;

import org.springframework.stereotype.Component;

/**
 * MQ重试策略
 * <p>18个重试级别，每个级别不同的延迟策略</p>
 */
@Component
public class MqRetryStrategy {

    /**
     * 18个级别的延迟时间（毫秒），默认写死
     */
    private static final long[] DELAY_MILLIS = {
            5 * 1000L,          // 第1次：5秒
            10 * 1000L,         // 第2次：10秒
            20 * 1000L,         // 第3次：20秒
            30 * 1000L,         // 第4次：30秒
            60 * 1000L,         // 第5次：1分钟
            2 * 60 * 1000L,     // 第6次：2分钟
            3 * 60 * 1000L,     // 第7次：3分钟
            5 * 60 * 1000L,     // 第8次：5分钟
            10 * 60 * 1000L,    // 第9次：10分钟
            15 * 60 * 1000L,    // 第10次：15分钟
            20 * 60 * 1000L,    // 第11次：20分钟
            30 * 60 * 1000L,    // 第12次：30分钟
            60 * 60 * 1000L,    // 第13次：1小时
            2 * 60 * 60 * 1000L,   // 第14次：2小时
            4 * 60 * 60 * 1000L,   // 第15次：4小时
            8 * 60 * 60 * 1000L,   // 第16次：8小时
            12 * 60 * 60 * 1000L,  // 第17次：12小时
            24 * 60 * 60 * 1000L   // 第18次：1天
    };

    /**
     * 最大重试次数
     */
    public static final int MAX_RETRY_COUNT = DELAY_MILLIS.length;

    /**
     * 获取指定重试次数的延迟时间
     *
     * @param retryCount 重试次数（1~18）
     * @return 延迟毫秒数
     */
    public long getDelayMillis(int retryCount) {
        if (retryCount <= 0) {
            return DELAY_MILLIS[0];
        }
        if (retryCount > DELAY_MILLIS.length) {
            return DELAY_MILLIS[DELAY_MILLIS.length - 1];
        }
        return DELAY_MILLIS[retryCount - 1];
    }
}
