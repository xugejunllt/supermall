package com.lanf.order.model.enums;


import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 售后订单主状态枚举
 */
@Getter
public enum MainStatusEnum {

    WAIT_SELLER_AGREE(0, "待审核"),
    WAIT_BUYER_RETURN(1, "待买家退货"),
    WAIT_SELLER_RECEIVE(2, "待收货"),
    WAIT_CONFIRM(3, "待退款/换货"),
    SUCCESS(4, "已完成"),
    CLOSED(5, "已关闭");

    /**
     * 售后中状态
     */
    public static final Set<Integer> PROCESSING_STATUS_SET = new HashSet<>(Arrays.asList(
            WAIT_SELLER_AGREE.code,
            WAIT_BUYER_RETURN.code,
            WAIT_SELLER_RECEIVE.code,
            WAIT_CONFIRM.code
    ));



    @EnumValue
    private final Integer code;
    private final String desc;



    MainStatusEnum(Integer code, String name) {
        this.code = code;
        this.desc = name;
    }
    @JsonValue
    public Integer getCode() {
        return code;
    }
    /**
     * 根据code获取枚举
     * @param code 记录类型代码
     * @return 对应的枚举值，如果未找到返回null
     */
    @JsonCreator
    public static MainStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (MainStatusEnum typeEnum : MainStatusEnum.values()) {
            if (code.equals(typeEnum.getCode())) {
                return typeEnum;
            }
        }
        return null;
    }


}
