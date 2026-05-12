package com.lanf.mybatis.utils;

import com.lanf.constant.utils.UserContext;
import com.lanf.mybatis.config.TenantEnableConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class OperatorUtils {


   @Autowired
   private  TenantEnableConfig tenantEnableConfig;
    /*
     * 是否启用租户 如果启用 那么就是后台系统用户
     */
    private static Boolean tenantEnable;

    @PostConstruct
    public void  init(){
        tenantEnable = tenantEnableConfig.getEnable();
    }

    /**
     * 系统默认操作人（用于定时任务、系统初始化等无法获取真实用户场景）
     */
    private static final String SYSTEM_OPERATOR = "sys:";
    /**
     * 后台管理系统
     */
    private static final String ADMIN_OPERATOR = "admin:";
    /**
     * C端用户
     */
    private static final String USER_OPERATOR = "user:";

    public static String getCurrentOperator() {


        Long userId = UserContext.getUserId();
        if (userId == null) {
            return SYSTEM_OPERATOR;
        }
        if (tenantEnable){
            return ADMIN_OPERATOR + userId;
        }
        return USER_OPERATOR + userId;
    }
}
