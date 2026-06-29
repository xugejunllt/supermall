package com.lanf.pay.model.enums;


import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 对账任务类型枚举
 */
@Getter
public enum ReconciliationJobTypeEnum {

    /**
     * 交易单长款扫描
     */
    TRADE_LONG_CHECK(0, "交易单长款扫描"),

    /**
     * 交易单短款扫描
     */
    TRADE_SHORT_CHECK(1, "交易单短款扫描"),

    /**
     * 退款单长款扫描
     */
    REFUND_LONG_CHECK(2, "退款单长款扫描"),

    /**
     * 退款单短款扫描
     */
    REFUND_SHORT_CHECK(3, "退款单短款扫描"),

    /**
     * 转账单长款扫描
     */
    TRANSFER_LONG_CHECK(4, "转账单长款扫描"),

    /**
     * 转账单短款扫描
     */
    TRANSFER_SHORT_CHECK(5, "转账单短款扫描");
    /**
     * 交易单和退款单任务类型集合
     */
    public static final Set<ReconciliationJobTypeEnum> TRADE_AND_REFUND_SET = Arrays.stream(values())
            .filter(type -> type == TRADE_LONG_CHECK
                    || type == TRADE_SHORT_CHECK
                    )
            .collect(Collectors.toSet());

    /**
     * 转账单任务类型集合
     */
    public static final Set<ReconciliationJobTypeEnum> TRANSFER_SET = Arrays.stream(values())
            .filter(type -> type == TRANSFER_LONG_CHECK
                    || type == TRANSFER_SHORT_CHECK)
            .collect(Collectors.toSet());


    @EnumValue
    @JsonValue
    private final Integer code;

    private final String desc;

    ReconciliationJobTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据code获取枚举
     *
     * @param code 任务类型编码
     * @return 枚举值
     */
    @JsonCreator
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

    /**
     * 判断是否为长款检查任务
     *
     * @return 是否为长款检查
     */
    public boolean isLongCheck() {
        return this == TRADE_LONG_CHECK 
            || this == REFUND_LONG_CHECK 
            || this == TRANSFER_LONG_CHECK;
    }

    /**
     * 判断是否为短款检查任务
     *
     * @return 是否为短款检查
     */
    public boolean isShortCheck() {
        return this == TRADE_SHORT_CHECK 
            || this == REFUND_SHORT_CHECK 
            || this == TRANSFER_SHORT_CHECK;
    }

    /**
     * 获取业务类型（交易/退款/转账）
     *
     * @return 业务类型描述
     */
    public String getBusinessType() {
        if (this == TRADE_LONG_CHECK || this == TRADE_SHORT_CHECK) {
            return "交易单";
        } else if (this == REFUND_LONG_CHECK || this == REFUND_SHORT_CHECK) {
            return "退款单";
        } else if (this == TRANSFER_LONG_CHECK || this == TRANSFER_SHORT_CHECK) {
            return "转账单";
        }
        return "未知";
    }
}
