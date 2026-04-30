package com.lanf.pay.model.enums;



import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 对账业务类型枚举
 */
@Getter
public enum ReconciliationBusinessTypeEnum {

    /**
     * 支付
     */
    PAYMENT(0, "支付"),

    /**
     * 退款
     */
    REFUND(1, "退款"),

    /**
     * 转账
     */
    TRANSFER(3, "转账");

    @EnumValue
    @JsonValue
    private final Integer code;

    private final String desc;

    ReconciliationBusinessTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据code获取枚举
     *
     * @param code 业务类型编码
     * @return 枚举值
     */
    @JsonCreator
    public static ReconciliationBusinessTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ReconciliationBusinessTypeEnum typeEnum : ReconciliationBusinessTypeEnum.values()) {
            if (code.equals(typeEnum.getCode())) {
                return typeEnum;
            }
        }
        return null;
    }
}
