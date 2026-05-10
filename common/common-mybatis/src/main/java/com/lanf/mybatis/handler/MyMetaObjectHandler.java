package com.lanf.mybatis.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.lanf.mybatis.config.TenantProperties;
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

//        if (tenantProperties.getFilterTables().contains(getTableName( metaObject) )) {
//            //拦截的租户表 插入租户id
//            Long merchantId = MerchantIdContext.getMerchantId();
//            this.setFieldValByName("tenantId",merchantId, metaObject);
//        }
    }

    private String getTableName(MetaObject metaObject) {

        Object originalObject = metaObject.getOriginalObject();
        TableInfo tableInfo = TableInfoHelper.getTableInfo(originalObject.getClass());

        return tableInfo.getTableName();

    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.setFieldValByName("updateTime", new Date(), metaObject);
    }
}
