package com.lanf.storage.model.enums;


import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum ReconciliationOrderStatusEnum {
    /**
     * 待出库：包含待支付、已支付、待出库状态。
     * 有用户侧库存扣减流水，无仓储侧库存流水。
     */
    PENDING_OUTBOUND(0, "待出库"),
    /**
     * 已出库：订单已发货出库。
     * 有用户侧库存扣减流水，有仓储侧库存流水。
     */
    OUTBOUNDED(1, "已出库"),
    /**
     * 已取消：订单已取消。
     * 有用户侧库存扣减流水，无仓储侧库存流水。
     */
    CANCELLED(2, "已取消"),


    ;
    @EnumValue
    @JsonValue
    private final Integer code;
    private final String name;




    ReconciliationOrderStatusEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }
    @JsonValue
    public Integer getCode() {
        return code;
    }

    public static ReconciliationOrderStatusEnum getOrderStatusEnum(Integer code) {
        for (ReconciliationOrderStatusEnum e : ReconciliationOrderStatusEnum.values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        return PENDING_OUTBOUND;
    }
    @JsonCreator
    public static ReconciliationOrderStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ReconciliationOrderStatusEnum typeEnum : ReconciliationOrderStatusEnum.values()) {
            if (code.equals(typeEnum.getCode())) {
                return typeEnum;
            }
        }
        return null;
    }
}
