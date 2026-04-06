package com.lanf.pay.model.enums;


import lombok.Getter;

@Getter
public enum CancelMergeEnum {

    NOT_CANCELLED(0, "未取消"),
    CANCELLED(1, "已取消");

    private Integer code;

    private String name;

    CancelMergeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }
}
