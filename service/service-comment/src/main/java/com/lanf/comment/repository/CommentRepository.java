package com.lanf.comment.repository;

import com.lanf.comment.model.document.CommentDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 评论 Repository（MongoDB）
 *
 * @author lanf
 */
@Repository
public interface CommentRepository extends MongoRepository<CommentDocument, String> {

    /**
     * 根据商品ID和评论类型分页查询
     */
    Page<CommentDocument> findByGoodsIdAndCommentTypeAndStatusAndIsDeleted(
            Long goodsId, Integer commentType, Integer status, Integer isDeleted, Pageable pageable);

    /**
     * 根据父评论ID和评论类型查询回复列表
     */
    List<CommentDocument> findByParentIdAndCommentTypeAndStatusAndIsDeletedOrderByCreateTimeDesc(
            Long parentId, Integer commentType, Integer status, Integer isDeleted);

    /**
     * 根据父评论ID分页查询回复列表
     */
    Page<CommentDocument> findByParentIdAndCommentTypeAndStatusAndIsDeleted(
            Long parentId, Integer commentType, Integer status, Integer isDeleted, Pageable pageable);

    /**
     * 统计商品的评论数量
     */
    long countByGoodsIdAndCommentTypeAndStatusAndIsDeleted(
            Long goodsId, Integer commentType, Integer status, Integer isDeleted);
}
