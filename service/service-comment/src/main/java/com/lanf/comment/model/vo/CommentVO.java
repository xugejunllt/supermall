package com.lanf.comment.model.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 评论 VO
 *
 * @author lanf
 */
@Data
public class CommentVO {

    /**
     * 评论ID
     */
    private Long commentId;

    /**
     * 商品ID
     */
    private Long goodsId;

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
     * 评论内容
     */
    private String content;

    /**
     * 评论图片
     */
    private List<String> images;

    /**
     * 评分
     */
    private Integer rating;

    /**
     * 点赞数
     */
    private Long likeCount;

    /**
     * 回复数
     */
    private Long replyCount;

    /**
     * 当前用户是否点赞
     */
    private Boolean likedByCurrentUser;

    /**
     * 置顶标记
     */
    private Integer topFlag;

    /**
     * 创建时间
     */
    private Date createTime;
}
