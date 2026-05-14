package com.lanf.api.storage.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 出库状态枚举
 */
@Getter
public enum StorageStatusEnum {

    WAIT_OUTBOUND(0, "待出库"),
    PARTIAL_OUTBOUND(1, "部分出库"),
    COMPLETED(2, "出库完成");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String name;

    StorageStatusEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }



    @JsonCreator
    public static StorageStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (StorageStatusEnum typeEnum : StorageStatusEnum.values()) {
            if (code.equals(typeEnum.getCode())) {
                return typeEnum;
            }
        }
        return null;
    }
}
