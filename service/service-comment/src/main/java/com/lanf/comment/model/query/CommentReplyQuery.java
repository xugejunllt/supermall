package com.lanf.comment.model.query;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 评论回复查询条件
 *
 * @author lanf
 */
@Data
public class CommentReplyQuery {

    /**
     * 父评论ID（一级评论ID）
     */
    @NotNull(message = "父评论ID不能为空")
    private Long parentId;

    /**
     * 页码
     */
    private Integer page = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;
}
