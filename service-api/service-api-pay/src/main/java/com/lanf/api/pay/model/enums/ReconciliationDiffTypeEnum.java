package com.lanf.api.pay.model.enums;


import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 对账差异类型枚举
 */
@Getter
public enum ReconciliationDiffTypeEnum {


    LONG(0, "长款", "长款：渠道有记录，我方无记录"),


    SHORT(1, "短款", "短款：渠道无记录，我方有记录"),

    /**
     * 金额不符：双方都有记录但金额不一致
     */
    AMOUNT_MISMATCH(2, "金额不符", "双方都有记录但金额不一致"),
    /**
     * 状态不一致：双方都有记录但交易状态不一致
     */
    STATUS_MISMATCH(3, "状态不一致", "双方都有记录但交易状态不一致");

    @EnumValue
    @JsonValue
    private final Integer code;

    private final String desc;

    private final String explanation;

    ReconciliationDiffTypeEnum(Integer code, String desc, String explanation) {
        this.code = code;
        this.desc = desc;
        this.explanation = explanation;
    }

    /**
     * 根据code获取枚举
     *
     * @param code 差异类型编码
     * @return 枚举值
     */
    @JsonCreator
    public static ReconciliationDiffTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ReconciliationDiffTypeEnum typeEnum : ReconciliationDiffTypeEnum.values()) {
            if (code.equals(typeEnum.getCode())) {
                return typeEnum;
            }
        }
        return null;
    }
}
