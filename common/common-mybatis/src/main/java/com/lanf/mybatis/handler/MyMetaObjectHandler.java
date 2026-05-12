package com.lanf.mybatis.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.lanf.mybatis.config.TenantProperties;
import com.lanf.mybatis.utils.OperatorUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Autowired
    private TenantProperties tenantProperties;

    @Override
    public void insertFill(MetaObject metaObject) {

        Date date = new Date();
        this.setFieldValByName("createTime", date, metaObject);
        this.setFieldValByName("updateTime", date, metaObject);
        this.setFieldValByName("isDeleted", 0, metaObject);
        this.setFieldValByName("createBy", OperatorUtils.getCurrentOperator(), metaObject);

    }



    @Override
    public void updateFill(MetaObject metaObject) {

        this.setFieldValByName("updateTime", new Date(), metaObject);
        this.setFieldValByName("updateBy", OperatorUtils.getCurrentOperator(), metaObject);

    }
}
