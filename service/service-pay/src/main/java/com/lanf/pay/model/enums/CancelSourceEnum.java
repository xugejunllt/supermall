package com.lanf.pay.model.enums;

import lombok.Getter;

/**
 * 订单取消来源枚举
 */
@Getter
public enum CancelSourceEnum {

    USER_MANUAL(0, "用户手动取消"),
    SYSTEM_TIMEOUT(1, "系统定时任务超时取消");

    private final Integer code;
    private final String description;

    CancelSourceEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }


}
