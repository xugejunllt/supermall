package com.lanf.web.utils;

public class UserContext {

    private static final ThreadLocal<Long> USER_ID_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<String> DEVICE_ID_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<Long> TENANT_ID_HOLDER = new ThreadLocal<>();

    /**
     * 设置用户ID
     *
     * @param userId 用户ID
     */
    public static void setUserId(Long userId) {
        USER_ID_HOLDER.set(userId);
    }

    /**
     * 获取用户ID
     *
     * @return 用户ID
     */
    public static Long getUserId() {
        return USER_ID_HOLDER.get();
    }

    /**
     * 设置设备ID
     *
     * @param deviceId 设备ID
     */
    public static void setDeviceId(String deviceId) {
        DEVICE_ID_HOLDER.set(deviceId);
    }

    /**
     * 获取设备ID
     *
     * @return 设备ID
     */
    public static String getDeviceId() {
        return DEVICE_ID_HOLDER.get();
    }

    /**
     * 设置租户ID
     *
     * @param tenantId 租户ID
     */
    public static void setTenantId(Long tenantId) {
        TENANT_ID_HOLDER.set(tenantId);
    }

    /**
     * 获取租户ID
     *
     * @return 租户ID
     */
    public static Long getTenantId() {
        return TENANT_ID_HOLDER.get();
    }

    /**
     * 清除所有上下文信息
     */
    public static void clear() {
        USER_ID_HOLDER.remove();
        DEVICE_ID_HOLDER.remove();
        TENANT_ID_HOLDER.remove();
    }
}
