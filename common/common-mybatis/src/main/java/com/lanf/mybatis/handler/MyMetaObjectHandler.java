package com.lanf.mybatis.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.lanf.common.utils.ThreadLocalUtils;
import com.lanf.constant.constant.Constants;
import com.lanf.security.utils.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {


    @Override
    public void insertFill(MetaObject metaObject) {
        Date date = new Date();
        this.setFieldValByName("createTime", date, metaObject);
        this.setFieldValByName("updateTime", date, metaObject);
        this.setFieldValByName("isDeleted", 0, metaObject);
        this.setFieldValByName("version", new Long("0"), metaObject);
        String tenantCode2 = UserUtil.getTenantCode();
        if ( tenantCode2 != null){
            log.info("新逻辑写入租户id");
            this.setFieldValByName("tenantCode", tenantCode2, metaObject);
            return;
        }
        String tenantCode1 = ThreadLocalUtils.getTenantCode();

        if (tenantCode1 != null){
            ThreadLocalUtils.removeTenantCodeThreadLocal();
            this.setFieldValByName("tenantCode", tenantCode1, metaObject);
            return;
        }

        this.setFieldValByName("tenantCode", Constants.ADMIN_TENANT_CODE, metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.setFieldValByName("updateTime", new Date(), metaObject);
    }
}
