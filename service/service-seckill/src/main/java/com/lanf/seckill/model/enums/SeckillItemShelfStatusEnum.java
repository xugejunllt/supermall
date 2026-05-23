package com.lanf.seckill.model.enums;


import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 秒杀商品上架状态枚举
 */
@Getter
public enum SeckillItemShelfStatusEnum {

    OFF_SHELF(0, "下架"),
    ON_SHELF(1, "上架");

    @EnumValue
    private final Integer code;
    private final String name;

    SeckillItemShelfStatusEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    @JsonValue
    public Integer getCode() {
        return code;
    }

    /**
     * 根据code获取枚举
     * @param code 状态代码
     * @return 对应的枚举值，如果未找到返回null
     */
    @JsonCreator
    public static SeckillItemShelfStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (SeckillItemShelfStatusEnum statusEnum : SeckillItemShelfStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum;
            }
        }
        return null;
    }
}
