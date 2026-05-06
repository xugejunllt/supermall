package com.lanf.storage.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 对账差异任务类型枚举
 */
@Getter
public enum ReconciliationJobTypeEnum {
    /**
     * 长库存扫描
     */
    LONG_STOCK_SCAN(0, "长库存扫描"),
    
    /**
     * 短库存扫描
     */
    SHORT_STOCK_SCAN(1, "短库存扫描");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String name;

    ReconciliationJobTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    @JsonValue
    public Integer getCode() {
        return code;
    }

    /**
     * 根据code获取枚举
     */
    public static ReconciliationJobTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ReconciliationJobTypeEnum typeEnum : ReconciliationJobTypeEnum.values()) {
            if (code.equals(typeEnum.getCode())) {
                return typeEnum;
            }
        }
        return null;
    }
}
