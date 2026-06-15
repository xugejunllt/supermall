package com.lanf.comment.repository;

import com.lanf.comment.model.document.CommentLikeDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

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
}
