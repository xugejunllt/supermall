package com.lanf.comment.repository;

import com.lanf.comment.model.document.CommentStatsDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 评论统计 Repository（MongoDB）
 *
 * @author lanf
 */
@Repository
public interface CommentStatsRepository extends MongoRepository<CommentStatsDocument, String> {

    /**
     * 根据评论ID查询统计数据
     */
    CommentStatsDocument findByCommentId(Long commentId);

    /**
     * 批量根据评论ID查询统计数据
     *
     * @param commentIds 评论ID列表
     * @return 统计数据列表
     */
    List<CommentStatsDocument> findByCommentIdIn(List<Long> commentIds);
}
