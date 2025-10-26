package com.lanf.common.utils;


import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ThreadLocalUtils {

    private static ThreadLocal<String> tenantCodeThreadLocal = new ThreadLocal<>();

    private static ThreadLocal<Boolean> ignoreTableNameThreadLocal = new ThreadLocal<>();

    public static void addTenantCode(String tenantCode) {

        tenantCodeThreadLocal.set(tenantCode);
    }

    public static void removeTenantCodeThreadLocal() {
        tenantCodeThreadLocal.remove();
    }

    public static String getTenantCode() {

        return tenantCodeThreadLocal.get();
    }

    public static void addIgnoreTableName(Boolean ignoreTableName) {

        ignoreTableNameThreadLocal.set(ignoreTableName);
    }

    public static Boolean getIgnoreTableName() {

        return ignoreTableNameThreadLocal.get();
    }

    public static void removeIgnoreTableName() {

        ignoreTableNameThreadLocal.remove();
    }

}
