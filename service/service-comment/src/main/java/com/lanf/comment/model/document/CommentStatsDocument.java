package com.lanf.comment.model.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * 评论统计数据文档（MongoDB）
 * <p>
 * 存储评论的点赞数、回复数等统计数据。
 * 采用独立文档存储，支持高并发场景下的原子更新（$inc）。
 * </p>
 *
 * <p><b>设计说明：</b></p>
 * <ul>
 *   <li>与 CommentDocument 分离，避免频繁更新导致文档膨胀</li>
 *   <li>使用 MongoDB $inc 操作符实现原子加减</li>
 *   <li>定时任务同步到 CommentDocument 冗余字段（如需要）</li>
 * </ul>
 *
 * <p><b>使用场景：</b></p>
 * <ul>
 *   <li>高并发点赞/取消点赞时原子更新 likeCount</li>
 *   <li>发布/删除回复时原子更新 replyCount</li>
 *   <li>统计热门评论排序</li>
 * </ul>
 *
 * @author lanf
 */
@Data
@Document(collection = "comment_stats")
public class CommentStatsDocument {

    /**
     * MongoDB 文档主键（String类型，自动生成）
     */
    @Id
    private String id;

    /**
     * 关联的评论ID（业务ID）
     */
    @Indexed(unique = true)
    private Long commentId;

    /**
     * 商品ID（冗余）
     */
    private Long goodsId;

    // ==================== 计数统计 ====================

    /**
     * 点赞数
     */
    private Long likeCount;

    /**
     * 回复数（仅一级评论有效）
     */
    private Long replyCount;

    // ==================== 扩展统计（可选） ====================

    /**
     * 点踩数
     */
    private Long dislikeCount;

    /**
     * 举报次数
     */
    private Integer reportCount;

    // ==================== 时间戳 ====================

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间（统计变更时刷新）
     */
    private Date updateTime;
}
