package com.lanf.common.utils;

import java.io.Serializable;
/**
 *忽略多租户拦截
 */
public class AutoIgnoreTenantContext implements Serializable {


    private static final ThreadLocal<Boolean> AUTO_IGNORE_MARK = new ThreadLocal<>();


    public static Boolean getAutoIgnoreMark() {
        return AUTO_IGNORE_MARK.get();
    }


    public static void setAutoIgnoreMark(Boolean mark) {
        AUTO_IGNORE_MARK.set(mark);
    }

    public static void removeAutoIgnoreMark() {
        AUTO_IGNORE_MARK.remove();
    }

}
