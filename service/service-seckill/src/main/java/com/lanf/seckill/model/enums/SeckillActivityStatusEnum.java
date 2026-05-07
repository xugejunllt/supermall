package com.lanf.seckill.model.enums;



import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 秒杀活动状态枚举
 */
@Getter
public enum SeckillActivityStatusEnum {

    NOT_STARTED(0, "未开始"),
    IN_PROGRESS(1, "进行中"),
    FINISHED(2, "已结束");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String name;

    SeckillActivityStatusEnum(Integer code, String name) {
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
    public static SeckillActivityStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (SeckillActivityStatusEnum statusEnum : SeckillActivityStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum;
            }
        }
        return null;
    }
}
