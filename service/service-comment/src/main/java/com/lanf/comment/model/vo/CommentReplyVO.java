package com.lanf.comment.model.vo;

import lombok.Data;

import java.util.Date;

/**
 * 评论回复 VO
 *
 * @author lanf
 */
@Data
public class CommentReplyVO {

    /**
     * 回复ID
     */
    private Long commentId;

    /**
     * 父评论ID
     */
    private Long parentId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 回复内容
     */
    private String content;

    /**
     * 被回复用户ID
     */
    private Long replyToUserId;

    /**
     * 被回复用户昵称
     */
    private String replyToUserName;

    /**
     * 点赞数
     */
    private Long likeCount;

    /**
     * 当前用户是否点赞
     */
    private Boolean likedByCurrentUser;

    /**
     * 创建时间
     */
    private Date createTime;
}
