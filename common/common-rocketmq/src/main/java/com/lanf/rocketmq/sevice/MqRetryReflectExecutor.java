package com.lanf.rocketmq.sevice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lanf.constant.exception.BizException;
import com.lanf.rocketmq.aspect.ExceptionFlagContext;
import com.lanf.rocketmq.exception.MessageRetryConsumeException;
import com.lanf.rocketmq.model.entity.MqConsumeMessageDO;
import com.lanf.rocketmq.model.enums.MqConsumeExceptionTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * MQ重试反射执行器
 * <p>通过反射重新执行被注解的方法</p>
 */
@Slf4j
@Component
public class MqRetryReflectExecutor {

    @Autowired
    private ApplicationContext applicationContext;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 通过反射执行重试方法
     *
     * @param messageDO 消息记录
     */
    public void execute(MqConsumeMessageDO messageDO) throws Exception {
        try {
            // 获取目标类
            Class<?> clazz = Class.forName(messageDO.getClassName());
            Object bean = applicationContext.getBean(clazz);
            // 解析参数类型
            String[] paramTypeNames = objectMapper.readValue(messageDO.getParamTypes(), String[].class);
            Class<?>[] paramTypes = new Class<?>[paramTypeNames.length];
            for (int i = 0; i < paramTypeNames.length; i++) {
                paramTypes[i] = getClassByName(paramTypeNames[i]);
            }

            // 解析参数值
            Object[] paramValues = new Object[paramTypes.length];
            if (messageDO.getParamValues() != null && !messageDO.getParamValues().isEmpty()) {
                Object[] rawValues = objectMapper.readValue(messageDO.getParamValues(), Object[].class);
                for (int i = 0; i < paramTypes.length && i < rawValues.length; i++) {
                    paramValues[i] = objectMapper.convertValue(rawValues[i], paramTypes[i]);
                }
            }

            // 反射调用方法（使用目标对象，绕过AOP代理）
            Method method = clazz.getMethod(messageDO.getMethodName(), paramTypes);
            ExceptionFlagContext.setReflectFlag(true);
            method.invoke(bean, paramValues);
            MqConsumeExceptionTypeEnum exceptionType = ExceptionFlagContext.getExceptionType();

            if ( exceptionType != null){

                if (MqConsumeExceptionTypeEnum.NEED_RETRY.equals(exceptionType)){
                    throw new MessageRetryConsumeException("反射执行失败");
                } else {
                    throw new BizException("反射执行失败");
                }
            }
            log.info("反射执行成功，messageId:{}, className:{}, methodName:{}",
                    messageDO.getMessageId(), messageDO.getClassName(), messageDO.getMethodName());
        } catch (Exception e) {
            log.error("反射执行失败，messageId:{}", messageDO.getMessageId(), e);
            throw  e;
        } finally {
            ExceptionFlagContext.clear();
            ExceptionFlagContext.clear();
        }
    }

    /**
     * 根据类名获取Class对象
     *
     * @param className 类名
     * @return Class对象
     * @throws ClassNotFoundException 类不存在
     */
    private Class<?> getClassByName(String className) throws ClassNotFoundException {
        switch (className) {
            case "byte":
                return byte.class;
            case "short":
                return short.class;
            case "int":
                return int.class;
            case "long":
                return long.class;
            case "float":
                return float.class;
            case "double":
                return double.class;
            case "boolean":
                return boolean.class;
            case "char":
                return char.class;
            default:
                return Class.forName(className);
        }
    }
}
