package com.lanf.comment.model.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 评论点赞/取消点赞 DTO
 *
 * @author lanf
 */
@Data
public class LikeCommentDTO {

    /**
     * 评论ID
     */
    @NotNull(message = "评论ID不能为空")
    private Long commentId;

    /**
     * 商品ID（冗余）
     */
    @NotNull(message = "商品ID不能为空")
    private Long goodsId;

    /**
     * 是否点赞：true-点赞，false-取消点赞
     */
    @NotNull(message = "点赞状态不能为空")
    private Boolean like;
}
