package com.lanf.lock.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {
    

    
    /**
     * 锁的key，支持SpEL表达式
     * 例如: #user.id, #orderNo, #args[0]
     */
    String key();
    

}