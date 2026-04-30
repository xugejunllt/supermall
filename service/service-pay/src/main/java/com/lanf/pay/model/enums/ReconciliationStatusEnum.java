package com.lanf.pay.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 对账状态枚举
 */
@Getter
public enum ReconciliationStatusEnum {

    /**
     * 处理中
     */
    PROCESSING(0, "处理中"),

    /**
     * 已完成
     */
    COMPLETED(2, "已完成");

    @EnumValue
    @JsonValue
    private final Integer code;

    private final String name;

    ReconciliationStatusEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 根据code获取枚举
     *
     * @param code 状态编码
     * @return 枚举值
     */
    @JsonCreator
    public static ReconciliationStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ReconciliationStatusEnum statusEnum : ReconciliationStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum;
            }
        }
        return null;
    }

    /**
     * 判断对账是否已完成
     *
     * @return 是否完成
     */
    public boolean isCompleted() {
        return this == COMPLETED;
    }

    /**
     * 判断对账是否正在处理中
     *
     * @return 是否处理中
     */
    public boolean isProcessing() {
        return this == PROCESSING;
    }
}
