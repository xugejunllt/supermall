package com.lanf.user.controller.app;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.lanf.api.user.api.UserApiService;
import com.lanf.common.utils.BeanUtil;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/app/sentinel")
public class SentinelTestController {
    @Autowired
    private RedissonClient redissonClient;


    @Autowired
    private UserApiService userApiService;

    @SentinelResource(
            value = "createOrder"
            , blockHandler = "createOrderBlockHandler"
//            , exceptionsToIgnore = {BizException.class}
    )
    @GetMapping("/exceptionTest")
    public Result<String> exceptionTest() {
        log.info("异常测试开始");
        // 模拟异常

        int i = 1 / 0;

        return Result.ok("异常测试成功");
    }

    /**
     * blockHandler 降级方法
     */
    public Result<String> createOrderBlockHandler(BlockException e) {

        log.error("触发降级方法");
        return Result.fail("熔断,接口降级");
    }

    @GetMapping("/feginTest")
    public Result<String> feginTest() {


        return userApiService.sentinelTest();
    }

    @GetMapping("/redisTest")
    public Result<String> redisTest() throws InterruptedException {

        SentinelTestController testController = BeanUtil.getBean(SentinelTestController.class);
        testController.set("String key", "String value", 1, TimeUnit.HOURS);

        return Result.ok("redis测试成功");
    }


    @SentinelResource(
            value = "fromRedis"
            , blockHandler = "fromRedisBlockHandler"
    )
    public void set(String key, String value, long expireTime, TimeUnit timeUnit) throws InterruptedException {

        Thread.sleep(1000);

        try {
            RBucket<String> bucket = redissonClient.getBucket(key);

            if (expireTime > 0) {
                bucket.set(value, expireTime, timeUnit);
                log.debug("设置缓存:key={},expire={}{}", key, expireTime, timeUnit);
            } else {
                bucket.set(value);
                log.debug("设置缓存(无过期):key={}", key);
            }
        } catch (Exception e) {
            log.error("设置缓存异常:key={}", key, e);
        }
    }

    public void fromRedisBlockHandler(String key, String value, long expireTime, TimeUnit timeUnit, BlockException e) {
        log.error("[Sentinel] Redis 操作触发降级，key={}", key);
        throw new BizException("Redis 操作触发降级");
    }
}
