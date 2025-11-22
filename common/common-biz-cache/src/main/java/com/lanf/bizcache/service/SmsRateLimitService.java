package com.lanf.bizcache.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class SmsRateLimitService {
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    private static final String SMS_RATE_LIMIT_KEY = "sms:rate:%s"; // %s 替换为手机号
    private static final long SMS_INTERVAL = 6000000; // 60秒内只能发送一次
    private static final long DAILY_LIMIT = 1000; // 每天最多发送10次
    
    public boolean canSend(String mobile) {
        String rateKey = String.format(SMS_RATE_LIMIT_KEY, mobile);
        String dailyCountKey = rateKey + ":daily";
        
        // 检查发送间隔
        if (redisTemplate.hasKey(rateKey)) {
            return false;
        }
        
        // 检查每日次数
        String dailyCountStr = redisTemplate.opsForValue().get(dailyCountKey);
        int dailyCount = dailyCountStr == null ? 0 : Integer.parseInt(dailyCountStr);
        if (dailyCount >= DAILY_LIMIT) {
            return false;
        }
        recordSend( mobile);
        return true;
    }
    
    private void recordSend(String mobile) {
        String rateKey = String.format(SMS_RATE_LIMIT_KEY, mobile);
        String dailyCountKey = rateKey + ":daily";
        
        // 设置发送间隔锁 过期时间60秒
        redisTemplate.opsForValue().set(rateKey, "1", Duration.ofSeconds(SMS_INTERVAL));
        
        // 更新每日计数
        redisTemplate.opsForValue().increment(dailyCountKey);
        // 设置每日计数过期时间（到当天结束）
        redisTemplate.expire(dailyCountKey, Duration.ofSeconds(getSecondsUntilMidnight()));
    }
    
    private long getSecondsUntilMidnight() {
        LocalDateTime midnight = LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0);
        return Duration.between(LocalDateTime.now(), midnight).getSeconds();
    }
}