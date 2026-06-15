package com.lanf.comment.service.impl;

import com.lanf.comment.model.document.CommentDocument;
import com.lanf.comment.model.document.CommentLikeDocument;
import com.lanf.comment.model.document.CommentStatsDocument;
import com.lanf.comment.model.dto.LikeCommentDTO;
import com.lanf.comment.model.dto.PublishCommentDTO;
import com.lanf.comment.model.dto.ReplyCommentDTO;
import com.lanf.comment.model.enums.CommentStatusEnum;
import com.lanf.comment.model.enums.CommentTypeEnum;
import com.lanf.comment.model.query.CommentPageQuery;
import com.lanf.comment.model.query.CommentReplyQuery;
import com.lanf.comment.model.vo.CommentReplyVO;
import com.lanf.comment.model.vo.CommentStatsVO;
import com.lanf.comment.model.vo.CommentVO;
import com.lanf.comment.repository.CommentLikeRepository;
import com.lanf.comment.repository.CommentRepository;
import com.lanf.comment.repository.CommentStatsRepository;
import com.lanf.comment.service.CommentService;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.utils.IdUtils;
import com.lanf.constant.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 评论 Service 实现
 *
 * @author lanf
 */
@Slf4j
@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    @Autowired
    private CommentStatsRepository commentStatsRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Long publishComment(PublishCommentDTO dto) {
        Long userId = UserContext.getUserId();
        log.info("发布评论, userId={}, goodsId={}", userId, dto.getGoodsId());

        Long commentId = IdUtils.generateId();
        Date now = new Date();

        CommentDocument comment = new CommentDocument();
        comment.setCommentId(commentId);
        comment.setGoodsId(dto.getGoodsId());
        comment.setOrderId(dto.getOrderId());
        comment.setSkuCode(dto.getSkuCode());
        comment.setUserId(userId);
        comment.setUserName(""); // TODO: 从用户服务获取
        comment.setUserAvatar(""); // TODO: 从用户服务获取
        comment.setContent(dto.getContent());
        comment.setImages(dto.getImages());
        comment.setRating(dto.getRating());
        comment.setCommentType(CommentTypeEnum.FIRST_LEVEL.getCode());
        comment.setParentId(0L);
        comment.setStatus(CommentStatusEnum.NORMAL.getCode());
        comment.setTopFlag(0);
        comment.setCreateTime(now);
        comment.setUpdateTime(now);
        comment.setIsDeleted(0);

        commentRepository.save(comment);

        // 初始化统计数据
        CommentStatsDocument stats = new CommentStatsDocument();
        stats.setCommentId(commentId);
        stats.setGoodsId(dto.getGoodsId());
        stats.setLikeCount(0L);
        stats.setReplyCount(0L);
        stats.setDislikeCount(0L);
        stats.setReportCount(0);
        stats.setCreateTime(now);
        stats.setUpdateTime(now);
        commentStatsRepository.save(stats);

        log.info("评论发布成功, commentId={}", commentId);
        return commentId;
    }

    @Override
    public Long replyComment(ReplyCommentDTO dto) {
        Long userId = UserContext.getUserId();
        log.info("回复评论, userId={}, parentId={}", userId, dto.getParentId());

        // 校验父评论是否存在
        CommentDocument parentComment = mongoTemplate.findOne(
                Query.query(Criteria.where("commentId").is(dto.getParentId())
                        .and("commentType").is(CommentTypeEnum.FIRST_LEVEL.getCode())
                        .and("status").is(CommentStatusEnum.NORMAL.getCode())
                        .and("isDeleted").is(0)),
                CommentDocument.class);

        if (parentComment == null) {
            log.warn("父评论不存在, parentId={}", dto.getParentId());
            throw new RuntimeException("评论不存在");
        }

        Long commentId = IdUtils.generateId();
        Date now = new Date();

        CommentDocument reply = new CommentDocument();
        reply.setCommentId(commentId);
        reply.setGoodsId(dto.getGoodsId());
        reply.setUserId(userId);
        reply.setUserName(""); // TODO: 从用户服务获取
        reply.setUserAvatar(""); // TODO: 从用户服务获取
        reply.setContent(dto.getContent());
        reply.setCommentType(CommentTypeEnum.SECOND_LEVEL.getCode());
        reply.setParentId(dto.getParentId());
        reply.setReplyToUserId(dto.getReplyToUserId());
        reply.setReplyToUserName(dto.getReplyToUserName());
        reply.setStatus(CommentStatusEnum.NORMAL.getCode());
        reply.setTopFlag(0);
        reply.setCreateTime(now);
        reply.setUpdateTime(now);
        reply.setIsDeleted(0);

        commentRepository.save(reply);

        // 更新父评论的回复数
        mongoTemplate.updateFirst(
                Query.query(Criteria.where("commentId").is(dto.getParentId())),
                new Update().inc("replyCount", 1),
                CommentDocument.class);

        // 更新统计文档的回复数
        mongoTemplate.updateFirst(
                Query.query(Criteria.where("commentId").is(dto.getParentId())),
                new Update().inc("replyCount", 1).set("updateTime", now),
                CommentStatsDocument.class);

        // 初始化回复的统计数据
        CommentStatsDocument stats = new CommentStatsDocument();
        stats.setCommentId(commentId);
        stats.setGoodsId(dto.getGoodsId());
        stats.setLikeCount(0L);
        stats.setReplyCount(0L);
        stats.setDislikeCount(0L);
        stats.setReportCount(0);
        stats.setCreateTime(now);
        stats.setUpdateTime(now);
        commentStatsRepository.save(stats);

        log.info("评论回复成功, commentId={}", commentId);
        return commentId;
    }

    @Override
    public void likeComment(LikeCommentDTO dto) {
        Long userId = UserContext.getUserId();
        Long commentId = dto.getCommentId();
        log.info("评论点赞/取消点赞, userId={}, commentId={}, like={}", userId, commentId, dto.getLike());

        if (dto.getLike()) {
            // 点赞
            CommentLikeDocument exist = commentLikeRepository.findByUserIdAndCommentId(userId, commentId);
            if (exist != null) {
                log.warn("用户已点赞, userId={}, commentId={}", userId, commentId);
                return;
            }

            CommentLikeDocument likeDoc = new CommentLikeDocument();
            likeDoc.setUserId(userId);
            likeDoc.setCommentId(commentId);
            likeDoc.setGoodsId(dto.getGoodsId());
            likeDoc.setCreateTime(new Date());
            commentLikeRepository.save(likeDoc);

            // 原子更新点赞数
            mongoTemplate.updateFirst(
                    Query.query(Criteria.where("commentId").is(commentId)),
                    new Update().inc("likeCount", 1),
                    CommentDocument.class);

            mongoTemplate.updateFirst(
                    Query.query(Criteria.where("commentId").is(commentId)),
                    new Update().inc("likeCount", 1).set("updateTime", new Date()),
                    CommentStatsDocument.class);
        } else {
            // 取消点赞
            commentLikeRepository.deleteByUserIdAndCommentId(userId, commentId);

            mongoTemplate.updateFirst(
                    Query.query(Criteria.where("commentId").is(commentId)),
                    new Update().inc("likeCount", -1),
                    CommentDocument.class);

            mongoTemplate.updateFirst(
                    Query.query(Criteria.where("commentId").is(commentId)),
                    new Update().inc("likeCount", -1).set("updateTime", new Date()),
                    CommentStatsDocument.class);
        }

        log.info("评论点赞操作完成, userId={}, commentId={}, like={}", userId, commentId, dto.getLike());
    }

    @Override
    public PageResult<CommentVO> queryCommentPage(CommentPageQuery query) {
        Long currentUserId = UserContext.getUserId();
        log.info("分页查询商品评论, goodsId={}", query.getGoodsId());

        Pageable pageable = PageRequest.of(query.getPage() - 1, query.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createTime"));

        Page<CommentDocument> page = commentRepository.findByGoodsIdAndCommentTypeAndStatusAndIsDeleted(
                query.getGoodsId(), CommentTypeEnum.FIRST_LEVEL.getCode(),
                CommentStatusEnum.NORMAL.getCode(), 0, pageable);

        List<CommentVO> records = page.getContent().stream()
                .map(doc -> convertToCommentVO(doc, currentUserId))
                .collect(Collectors.toList());

        PageResult<CommentVO> result = new PageResult<>();
        result.setRecords(records);
        result.setTotal(page.getTotalElements());
        result.setSize(page.getSize());
        return result;
    }

    @Override
    public PageResult<CommentReplyVO> queryReplyPage(CommentReplyQuery query) {
        Long currentUserId = UserContext.getUserId();
        log.info("分页查询评论回复, parentId={}", query.getParentId());

        Pageable pageable = PageRequest.of(query.getPage() - 1, query.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createTime"));

        Page<CommentDocument> page = commentRepository.findByParentIdAndCommentTypeAndStatusAndIsDeleted(
                query.getParentId(), CommentTypeEnum.SECOND_LEVEL.getCode(),
                CommentStatusEnum.NORMAL.getCode(), 0, pageable);

        List<CommentReplyVO> records = page.getContent().stream()
                .map(doc -> convertToReplyVO(doc, currentUserId))
                .collect(Collectors.toList());

        PageResult<CommentReplyVO> result = new PageResult<>();
        result.setRecords(records);
        result.setTotal(page.getTotalElements());
        result.setSize(page.getSize());
        return result;
    }

    @Override
    public CommentStatsVO getCommentStats(Long commentId) {
        CommentStatsDocument stats = commentStatsRepository.findByCommentId(commentId);
        if (stats == null) {
            CommentStatsVO vo = new CommentStatsVO();
            vo.setCommentId(commentId);
            vo.setLikeCount(0L);
            vo.setReplyCount(0L);
            return vo;
        }
        return BeanCopyUtils.copyBean(stats, CommentStatsVO.class);
    }

    @Override
    public Long countGoodsComment(Long goodsId) {
        return commentRepository.countByGoodsIdAndCommentTypeAndStatusAndIsDeleted(
                goodsId, CommentTypeEnum.FIRST_LEVEL.getCode(),
                CommentStatusEnum.NORMAL.getCode(), 0);
    }

    // ==================== 私有转换方法 ====================

    private CommentVO convertToCommentVO(CommentDocument doc, Long currentUserId) {
        CommentVO vo = new CommentVO();
        vo.setCommentId(doc.getCommentId());
        vo.setGoodsId(doc.getGoodsId());
        vo.setUserId(doc.getUserId());
        vo.setUserName(doc.getUserName());
        vo.setUserAvatar(doc.getUserAvatar());
        vo.setContent(doc.getContent());
        vo.setImages(doc.getImages());
        vo.setRating(doc.getRating());
        vo.setTopFlag(doc.getTopFlag());
        vo.setCreateTime(doc.getCreateTime());

        if (currentUserId != null) {
            vo.setLikedByCurrentUser(
                    commentLikeRepository.existsByUserIdAndCommentId(currentUserId, doc.getCommentId()));
        } else {
            vo.setLikedByCurrentUser(false);
        }
        return vo;
    }

    private CommentReplyVO convertToReplyVO(CommentDocument doc, Long currentUserId) {
        CommentReplyVO vo = new CommentReplyVO();
        vo.setCommentId(doc.getCommentId());
        vo.setParentId(doc.getParentId());
        vo.setUserId(doc.getUserId());
        vo.setUserName(doc.getUserName());
        vo.setUserAvatar(doc.getUserAvatar());
        vo.setContent(doc.getContent());
        vo.setReplyToUserId(doc.getReplyToUserId());
        vo.setReplyToUserName(doc.getReplyToUserName());
        vo.setCreateTime(doc.getCreateTime());

        if (currentUserId != null) {
            vo.setLikedByCurrentUser(
                    commentLikeRepository.existsByUserIdAndCommentId(currentUserId, doc.getCommentId()));
        } else {
            vo.setLikedByCurrentUser(false);
        }
        return vo;
    }
}
