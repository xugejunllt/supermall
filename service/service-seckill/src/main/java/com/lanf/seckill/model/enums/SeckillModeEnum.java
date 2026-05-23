package com.lanf.seckill.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum SeckillModeEnum {
    /**
     *
     * 低价值商品 允许超卖 特点：响应快
     *
     * 实际扣减冻结库存时 可能正常用户 下单了 但是冻结库存被扣减为0了
     * 所以避免这种情况发生 提前将普通商品进行下架
     *
     */
    REAL_TIME(0, "实时秒杀"),
    /**
     * 高价值商品 不允许超卖
     */
    MQ_QUEUE(1, "MQ排队秒杀");
    
    @EnumValue
    private final Integer code;
    private final String name;
    
    SeckillModeEnum(Integer code, String name) {
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
    public static SeckillModeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (SeckillModeEnum statusEnum : SeckillModeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum;
            }
        }
        return null;
    }
}
