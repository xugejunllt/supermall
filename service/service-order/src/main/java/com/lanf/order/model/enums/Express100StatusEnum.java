package com.lanf.order.model.enums;

import lombok.Getter;

/**
 * 快递100物流状态枚举（与官方接口文档一致）

 */
@Getter
public enum Express100StatusEnum {

    /**
     * 0：运输中（途中）
     */
    IN_TRANSIT(0, "运输中"),

    /**
     * 1：已揽收
     */
    COLLECTED(1, "已揽收"),

    /**
     * 2：疑难件（异常）
     */
    EXCEPTION(2, "疑难件"),

    /**
     * 3：已签收
     */
    SIGNED(3, "已签收"),

    /**
     * 4：退签
     */
    RETURN_SIGNED(4, "退签"),

    /**
     * 5：派送中
     */
    DELIVERING(5, "派送中"),

    /**
     * 6：退回
     */
    RETURNED(6, "退回"),

    /**
     * 7：转投
     */
    REDIRECTED(7, "转投"),

    /**
     * 8：清关
     */
    CUSTOMS_CLEARANCE(8, "清关"),

    /**
     * 9：已下单（待揽件）
     */
    ORDER_PLACED(9, "已下单"),

    /**
     * 10：待派件
     */
    WAITING_DELIVERY(10, "待派件"),

    /**
     * 14：拒收
     */
    REJECTED(14, "拒收");

    private final Integer code;
    private final String desc;

    Express100StatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据code获取枚举
     *
     * @param code 状态码
     * @return 对应的枚举值，如果未找到返回null
     */
    public static Express100StatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (Express100StatusEnum e : Express100StatusEnum.values()) {
            if (e.code.equals(code)) {
                return e;
            }
        }
        return null;
    }
}
