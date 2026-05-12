package com.lanf.mybatis.utils;

public class TenantContextHolder {
    private static final ThreadLocal<Boolean> SKIP_TENANT = new ThreadLocal<>();

    public static void setSkipTenant(boolean skip) {
        SKIP_TENANT.set(skip);
    }

    public static boolean isSkipTenant() {
        return SKIP_TENANT.get() != null && SKIP_TENANT.get();
    }

    public static void clear() {
        SKIP_TENANT.remove();
    }
}