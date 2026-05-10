package com.lanf.constant.model.enums;



import lombok.Getter;

@Getter
public enum FrozenStatusEnum {

    NORMAL(0, "正常"),
    FROZEN(1, "冻结状态");

    private Integer code;

    private String name;

    FrozenStatusEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

}
