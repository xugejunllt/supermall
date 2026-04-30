package com.lanf.pay.model.enums;


import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 对账任务状态枚举
 */
@Getter
public enum ReconciliationJobStatusEnum {

    /**
     * 执行中
     */
    EXECUTING(0, "执行中"),

    /**
     * 已完成
     */
    COMPLETED(2, "已完成");

    @EnumValue
    @JsonValue
    private final Integer code;

    private final String desc;

    ReconciliationJobStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据code获取枚举
     *
     * @param code 状态编码
     * @return 枚举值
     */
    @JsonCreator
    public static ReconciliationJobStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ReconciliationJobStatusEnum statusEnum : ReconciliationJobStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum;
            }
        }
        return null;
    }

    /**
     * 判断任务是否已完成
     *
     * @return 是否完成
     */
    public boolean isCompleted() {
        return this == COMPLETED;
    }

    /**
     * 判断任务是否正在执行
     *
     * @return 是否执行中
     */
    public boolean isExecuting() {
        return this == EXECUTING;
    }
}
