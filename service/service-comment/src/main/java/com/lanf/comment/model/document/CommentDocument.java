package com.lanf.comment.model.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

/**
 * 评论文档（MongoDB）
 * <p>
 * 存储商品评论及回复，采用单层嵌套设计（类似抖音）：
 * <ul>
 *   <li>一级评论：用户对商品的直接评论（parentId = 0）</li>
 *   <li>二级回复：对一级评论的回复（parentId = 一级评论的commentId）</li>
 * </ul>
 * 不支持回复的回复（三级及以上嵌套）。
 * </p>
 *
 * <p><b>索引设计：</b></p>
 * <ul>
 *   <li>goodsId + commentType + createTime：查询商品下某类型评论</li>
 *   <li>parentId + createTime：查询某一级评论下的回复列表</li>
 *   <li>userId：查询用户的所有评论</li>
 * </ul>
 *
 * @author lanf
 */
@Data
@Document(collection = "comment")
@CompoundIndex(name = "idx_goods_type_time", def = "{'goodsId': 1, 'commentType': 1, 'createTime': -1}")
@CompoundIndex(name = "idx_parent_time", def = "{'parentId': 1, 'createTime': -1}")
public class CommentDocument {

    // ==================== 主键 ====================

    /**
     * MongoDB 文档主键（String类型，自动生成）
     */
    @Id
    private String id;

    /**
     * 业务评论ID（Long类型，雪花算法生成，用于业务关联）
     */
    @Indexed
    private Long commentId;

    // ==================== 关联信息 ====================

    /**
     * 商品ID（评论所属商品）
     */
    @Indexed
    private Long goodsId;

    /**
     * 订单ID（关联订单，可选：用于订单商品评论）
     */
    private Long orderId;

    /**
     * 订单SKU编码（用于区分同一商品不同SKU的评论）
     */
    private String skuCode;

    // ==================== 评论人信息（冗余缓存） ====================

    /**
     * 评论用户ID
     */
    @Indexed
    private Long userId;

    /**
     * 评论用户昵称（冗余，避免频繁查用户表）
     */
    private String userName;

    /**
     * 评论用户头像（冗余）
     */
    private String userAvatar;

    // ==================== 评论内容 ====================

    /**
     * 评论内容（文本，支持Emoji）
     */
    private String content;

    /**
     * 评论图片列表（可选，最多9张）
     */
    private List<String> images;

    /**
     * 评论评分（1-5星，可选）
     */
    private Integer rating;

    // ==================== 单层嵌套回复结构 ====================

    /**
     * 评论类型：1-一级评论，2-二级回复
     *
     * @see com.lanf.comment.model.enums.CommentTypeEnum
     */
    private Integer commentType;

    /**
     * 父评论ID
     * <ul>
     *   <li>一级评论：parentId = 0</li>
     *   <li>二级回复：parentId = 被回复的一级评论的commentId</li>
     * </ul>
     */
    private Long parentId;

    /**
     * 被回复用户ID（二级回复才有，即回复给谁）
     */
    private Long replyToUserId;

    /**
     * 被回复用户昵称（二级回复才有）
     */
    private String replyToUserName;

    // ==================== 统计数据（冗余，便于排序展示） ====================

    /**
     * 点赞数（冗余字段，实时性要求不高时可直接读取）
     */
    private Long likeCount;

    /**
     * 回复数（仅一级评论有效，记录该评论下的二级回复数量）
     */
    private Long replyCount;

    // ==================== 状态管理 ====================

    /**
     * 评论状态：1-正常，2-隐藏，3-已删除
     *
     * @see com.lanf.comment.model.enums.CommentStatusEnum
     */
    private Integer status;

    /**
     * 置顶标记：0-普通，1-置顶
     */
    private Integer topFlag;

    // ==================== 时间戳 ====================

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
