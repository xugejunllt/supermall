package com.lanf.comment.model.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * 评论点赞记录文档（MongoDB）
 * <p>
 * 记录用户对评论的点赞关系，实现幂等点赞/取消点赞。
 * 复合索引 (userId + commentId) 保证唯一性，防止重复点赞。
 * </p>
 *
 * <p><b>使用场景：</b></p>
 * <ul>
 *   <li>用户点赞时插入记录</li>
 *   <li>用户取消点赞时删除记录</li>
 *   <li>查询用户是否点赞过某条评论</li>
 *   <li>统计评论的点赞用户列表（可选）</li>
 * </ul>
 *
 * @author lanf
 */
@Data
@Document(collection = "comment_like")
@CompoundIndex(name = "idx_user_comment", def = "{'userId': 1, 'commentId': 1}", unique = true)
public class CommentLikeDocument {

    /**
     * MongoDB 文档主键
     */
    @Id
    private String id;

    /**
     * 点赞用户ID
     */
    private Long userId;

    /**
     * 被点赞的评论ID
     */
    private Long commentId;

    /**
     * 被点赞的商品ID（冗余，便于统计商品维度数据）
     */
    private Long goodsId;

    /**
     * 点赞时间
     */
    private Date createTime;
}
