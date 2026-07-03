package com.lanf.comment.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 评论文档分页查询返回VO
 *
 * @author lanf
 */
@Data
public class CommentDocumentPageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评论ID
     */
    private Long commentId;

    /**
     * 商品ID
     */
    private Long goodsId;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单SKU编码
     */
    private String skuCode;

    /**
     * 评论用户ID
     */
    private Long userId;

    /**
     * 评论用户昵称
     */
    private String userName;

    /**
     * 评论用户头像
     */
    private String userAvatar;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 评论图片列表
     */
    private List<String> images;

    /**
     * 评论评分（1-5星）
     */
    private Integer rating;

    /**
     * 评论类型：1-一级评论，2-二级回复
     */
    private Integer commentType;

    /**
     * 父评论ID
     */
    private Long parentId;

    /**
     * 被回复用户ID
     */
    private Long replyToUserId;

    /**
     * 被回复用户昵称
     */
    private String replyToUserName;

    /**
     * 评论状态：1-正常，2-隐藏，3-已删除
     */
    private Integer status;

    /**
     * 置顶标记：0-普通，1-置顶
     */
    private Integer topFlag;

    /**
     * 点赞数
     */
    private Long likeCount;

    /**
     * 回复数
     */
    private Long replyCount;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除（逻辑删除）：0-未删除，1-已删除
     */
    private Integer isDeleted;

}
