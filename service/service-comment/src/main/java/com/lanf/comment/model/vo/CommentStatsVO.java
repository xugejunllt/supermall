package com.lanf.comment.model.vo;

import lombok.Data;

/**
 * 评论统计数据 VO
 *
 * @author lanf
 */
@Data
public class CommentStatsVO {

    /**
     * 评论ID
     */
    private Long commentId;

    /**
     * 点赞数
     */
    private Long likeCount;

    /**
     * 回复数
     */
    private Long replyCount;
}
