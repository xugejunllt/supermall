package com.lanf.common.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 *
 * 不要在bean对象的构造方法中使用 此时 ApplicationContextAware Bean还
 * 未被初始化
 * 1. 实例化Bean（调用构造方法）
 * 2. 填充属性（依赖注入）
 * 3. 调用Aware接口方法（包括ApplicationContextAware）--问题在这里  BeanUtil可能还没有初始化
 * 4. BeanPostProcessor前置处理
 * 5. 初始化方法（@PostConstruct）
 * 6. BeanPostProcessor后置处理
 * 7. Bean准备就绪
 *
 */
@Component
public class BeanUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext actx) throws BeansException {
        if (applicationContext == null) {
            applicationContext = actx;
        }
    }

    /**
     * 获取applicationContext
     *
     * @return
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }


    /**
     * 通过name获取bean
     *
     * @param name
     * @return
     */
    public static Object getBean(String name) {
        return getApplicationContext().getBean(name);
    }

    /**
     * 通过class获取bean
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T getBean(Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }

    /**
     * 通过name,以及clazz返回的指定的Bean
     *
     * @param name
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return getApplicationContext().getBean(name, clazz);

    }
}
