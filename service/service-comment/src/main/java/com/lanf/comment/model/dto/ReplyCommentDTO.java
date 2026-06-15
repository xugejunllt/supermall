package com.lanf.comment.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 回复评论 DTO
 *
 * @author lanf
 */
@Data
public class ReplyCommentDTO {

    /**
     * 商品ID
     */
    @NotNull(message = "商品ID不能为空")
    private Long goodsId;

    /**
     * 父评论ID（被回复的一级评论ID）
     */
    @NotNull(message = "父评论ID不能为空")
    private Long parentId;

    /**
     * 被回复用户ID
     */
    @NotNull(message = "被回复用户ID不能为空")
    private Long replyToUserId;

    /**
     * 被回复用户昵称
     */
    @NotBlank(message = "被回复用户昵称不能为空")
    private String replyToUserName;

    /**
     * 回复内容
     */
    @NotBlank(message = "回复内容不能为空")
    private String content;
}
