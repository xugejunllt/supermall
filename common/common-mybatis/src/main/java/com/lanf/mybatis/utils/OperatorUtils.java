package com.lanf.mybatis.utils;

import com.lanf.constant.utils.UserContext;
import org.springframework.stereotype.Component;

@Component
public class OperatorUtils {





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
        Boolean admin = UserContext.getAdmin();
        if (userId == null && admin == null) {
            return SYSTEM_OPERATOR;
        }
        if (userId !=null && Boolean.TRUE.equals( admin)){
            return ADMIN_OPERATOR + userId;
        }
        return USER_OPERATOR + userId;
    }
}
