package com.lanf.mybatis.handler;

import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.lanf.common.utils.IdUtils;
import org.springframework.context.annotation.Configuration;

/**
 * 自定义id生成策略
 */
@Configuration
public class IIdentifierGenerator implements IdentifierGenerator {


    @Override
    public Number nextId(Object entity) {

        return IdUtils.generateId();
    }
}
