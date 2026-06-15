package com.lanf.comment.repository;

import com.lanf.comment.model.document.CommentLikeDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 评论点赞 Repository（MongoDB）
 *
 * @author lanf
 */
@Repository
public interface CommentLikeRepository extends MongoRepository<CommentLikeDocument, String> {

    /**
     * 根据用户ID和评论ID查询点赞记录
     */
    CommentLikeDocument findByUserIdAndCommentId(Long userId, Long commentId);

    /**
     * 根据用户ID和评论ID删除点赞记录
     */
    void deleteByUserIdAndCommentId(Long userId, Long commentId);

    /**
     * 判断用户是否点赞过某条评论
     */
    boolean existsByUserIdAndCommentId(Long userId, Long commentId);

    /**
     * 根据商品ID和用户ID查询点赞记录（用于 Redis Set 初始化）
     *
     * @param goodsId 商品ID
     * @param userId  用户ID
     * @return 点赞记录列表
     */
    List<CommentLikeDocument> findByGoodsIdAndUserId(Long goodsId, Long userId);
}
