package com.lanf.api.user.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 用户状态枚举
 */
@Getter
public enum UserStatusEnum {

    /**
     * 待审核
     */
    PENDING_REVIEW(0, "待审核"),

    /**
     * 正常
     */
    NORMAL(1, "正常"),

    /**
     * 禁用
     */
    DISABLED(2, "禁用");

    /**
     * 数据库存储的值
     */
    @EnumValue
    private final Integer code;

    /**
     * JSON序列化时的值
     */
    private final String description;

    UserStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }
    @JsonValue
    public Integer getCode() {
        return code;
    }
    /**
     * 根据code获取枚举
     * 
     * @param code 状态码
     * @return 枚举值，如果不存在则返回null
     */
    public static UserStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        
        for (UserStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        
        return null;
    }
}
