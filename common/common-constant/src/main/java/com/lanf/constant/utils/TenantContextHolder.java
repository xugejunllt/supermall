package com.lanf.constant.utils;

public class TenantContextHolder {
    private static final ThreadLocal<Boolean> SKIP_TENANT = new ThreadLocal<>();

    public static void setSkipTenant(boolean skip) {
        SKIP_TENANT.set(skip);
    }

    public static boolean isSkipTenant() {
        Boolean skipTenant = SKIP_TENANT.get();
        return skipTenant == null || skipTenant;
    }

    public static void clear() {
        SKIP_TENANT.remove();
    }
}