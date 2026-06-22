package com.lanf.seckill.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 秒杀优惠券记录状态枚举
 */
@Getter
public enum SecKillCouponRecordStatusEnum {

    /**
     * 0: 秒杀成功
     */
    SUCCESS(0, "秒杀成功"),

    /**
     * 1: 优惠券已发放
     */
    COUPON_ISSUED(1, "优惠券已发放"),

    /**
     * 2: 秒杀失败
     */
    FAILED(2, "秒杀失败");

    @EnumValue
    private final Integer code;

    private final String name;

    SecKillCouponRecordStatusEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    @JsonValue
    public Integer getCode() {
        return code;
    }

    /**
     * 根据code获取枚举
     *
     * @param code 状态代码
     * @return 对应的枚举值，如果未找到返回null
     */
    @JsonCreator
    public static SecKillCouponRecordStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (SecKillCouponRecordStatusEnum statusEnum : SecKillCouponRecordStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum;
            }
        }
        return null;
    }
}
