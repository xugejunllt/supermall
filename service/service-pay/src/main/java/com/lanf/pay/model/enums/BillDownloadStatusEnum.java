package com.lanf.pay.model.enums;


import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 对账单下载状态枚举
 */
@Getter
public enum BillDownloadStatusEnum {



    /**
     * 初始化
     */
    INIT(0, "初始化"),

    /**
     * 下载中
     */
    DOWNLOADING(1, "下载中"),

    /**
     * 下载完成
     */
    COMPLETED(2, "下载完成");

    @EnumValue
    @JsonValue
    private final Integer code;

    private final String desc;

    BillDownloadStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据code获取枚举
     *
     * @param code 状态码
     * @return 枚举值
     */
    @JsonCreator
    public static BillDownloadStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (BillDownloadStatusEnum statusEnum : BillDownloadStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum;
            }
        }
        return null;
    }
}
