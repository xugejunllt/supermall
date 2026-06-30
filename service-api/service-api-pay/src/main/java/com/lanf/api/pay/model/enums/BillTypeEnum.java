package com.lanf.api.pay.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 账单类型枚举
 */
@Getter
public enum BillTypeEnum {

    /**
     * 签约客户账单
     */
    SIGN_CUSTOMER("signcustomer", "签约客户账单"),

    /**
     * 交易账单
     */
    TRADE("trade", "交易账单");

    @EnumValue
    @JsonValue
    private final String code;

    private final String desc;

    BillTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 根据code获取枚举
     *
     * @param code 账单类型编码
     * @return 枚举值
     */
    @JsonCreator
    public static BillTypeEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (BillTypeEnum billType : BillTypeEnum.values()) {
            if (code.equals(billType.getCode())) {
                return billType;
            }
        }
        return null;
    }
}
