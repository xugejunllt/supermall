package com.lanf.comment.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 评论状态枚举
 *
 * @author lanf
 */
@Getter
public enum CommentStatusEnum {

    /**
     * 正常展示
     */
    NORMAL(1, "正常"),

    /**
     * 隐藏（如被举报、违规等）
     */
    HIDDEN(2, "隐藏"),

    /**
     * 已删除
     */
    DELETED(3, "已删除");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String name;

    CommentStatusEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    @JsonCreator
    public static CommentStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (CommentStatusEnum statusEnum : CommentStatusEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum;
            }
        }
        return null;
    }
}
