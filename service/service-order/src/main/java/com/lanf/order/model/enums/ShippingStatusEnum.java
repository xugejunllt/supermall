package com.lanf.order.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 物流状态枚举
 */
@Getter
public enum ShippingStatusEnum {

    ORDER_PLACED(0, "已下单", 1),
    WAREHOUSE_PROCESSING(1, "仓库处理中", 2),
    COLLECTED(2, "已揽收", 3),
    IN_TRANSIT(3, "运输中", 4),
    DELIVERING(4, "派送中", 5),
    SIGNED(5, "已签收", 6);

    /**
     * 状态码
     */
    @EnumValue
    private final Integer code;
    /**
     * 状态名称
     */
    private final String name;
    /**
     * 排序号（越小越靠前）
     */
    private final Integer sort;

    ShippingStatusEnum(Integer code, String name, Integer sort) {
        this.code = code;
        this.name = name;
        this.sort = sort;
    }

    @JsonValue
    public Integer getCode() {
        return code;
    }
    /**
     * 根据 code 获取枚举
     *
     * @param code 状态码
     * @return 对应的枚举值，如果未找到返回 null
     */
    @JsonCreator
    public static ShippingStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ShippingStatusEnum e : ShippingStatusEnum.values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        return null;
    }
}
