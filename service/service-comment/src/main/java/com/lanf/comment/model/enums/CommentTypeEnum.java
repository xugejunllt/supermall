package com.lanf.comment.model.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 评论类型枚举
 * <p>
 * 用于区分一级评论和二级回复
 * 抖音风格：仅支持单层嵌套回复（一级评论 + 对一级评论的回复）
 * </p>
 *
 * @author lanf
 */
@Getter
public enum CommentTypeEnum {

    /**
     * 一级评论：用户对商品的直接评论
     */
    FIRST_LEVEL(1, "一级评论"),

    /**
     * 二级回复：对一级评论的回复（单层嵌套）
     */
    SECOND_LEVEL(2, "二级回复");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String name;

    CommentTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    @JsonCreator
    public static CommentTypeEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (CommentTypeEnum typeEnum : CommentTypeEnum.values()) {
            if (code.equals(typeEnum.getCode())) {
                return typeEnum;
            }
        }
        return null;
    }
}
