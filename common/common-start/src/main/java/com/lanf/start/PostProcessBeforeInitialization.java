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
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
        init = true;
        return bean;
    }
}
