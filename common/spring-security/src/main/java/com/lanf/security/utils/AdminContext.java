package com.lanf.security.utils;


import com.lanf.security.model.AdminCacheBO;

/**
 * 用户上下文工具类
 * 用于在同一个线程中共享用户信息
 */
public class AdminContext {

    private static final ThreadLocal<AdminCacheBO> ADMIN_CONTEXT = new ThreadLocal<>();

    public static void set(AdminCacheBO adminCacheBO) {

        ADMIN_CONTEXT.set(adminCacheBO);
    }

    public static AdminCacheBO get() {

        return ADMIN_CONTEXT.get();
    }


    public static void clear() {
        ADMIN_CONTEXT.remove();
    }

}