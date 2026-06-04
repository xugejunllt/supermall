package com.lanf.start;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

/**
 * 在容器所有bean初始化完前执行 保证还没有使用自定义bean对象前就已经完成流一些操作流 避免时区还没设置完 就执行了数据库操作
 * 比如设置时区
 */
@Slf4j
@Configuration
public class PostProcessBeforeInitialization implements BeanPostProcessor {

    private boolean init = false;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (init) {

            return bean;
        }
        //将 JVM 的默认时区设置为东八区（北京时间）
        //代码里调用 new Date()、SimpleDateFormat 格式化、LocalDateTime.now()
        // 等操作，如果没有显式指定时区，都会默认使用北京时间而不是服务器本地时间。
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
        init = true;
        return bean;
    }
}
