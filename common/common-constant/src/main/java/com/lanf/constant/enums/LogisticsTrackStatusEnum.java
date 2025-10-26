package com.lanf.constant.enums;

import lombok.Getter;

/**
 * 与快递100状态码一致 其他状态码都视为异常
 */
@Getter
public enum LogisticsTrackStatusEnum {

    PLACE_AN_ORDER_BUS_INCOME(9, "已下单"),
    PLACE_AN_ORDER_PLATFORM_INCOME(10, "仓库处理中"),
    COLLECTED_ALREADY(1, "已发货"),
    IN_TRANSIT(0, "运输中"),
    DELIVER_GOODS(5, "派送中"),
    SIGNED_FOR(3, "已签收"),
    EXPRESS_DELIVERY_EXCEPTION(2, "快递异常");

    private final Integer code;
    private final String name;

    //收入支出 0:收入 1:支出
    LogisticsTrackStatusEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

   public static LogisticsTrackStatusEnum getLogisticsTrackStatusEnum(Integer code) {

        for (LogisticsTrackStatusEnum e : LogisticsTrackStatusEnum.values()) {
            if (e.code.equals(code)) {

                return e;
            }
        }
        //其他状态都视为异常 如果出现异常 人工查询完整的快递信息，并手动修改轨迹信息
        return EXPRESS_DELIVERY_EXCEPTION;
    }


}
