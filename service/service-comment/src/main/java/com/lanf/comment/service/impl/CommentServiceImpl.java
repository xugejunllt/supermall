package com.lanf.comment.service.impl;

import com.lanf.api.order.mq.message.PublishCommentMessage;
import com.lanf.comment.model.document.CommentDocument;
import com.lanf.comment.model.document.CommentStatsDocument;
import com.lanf.comment.model.dto.LikeCommentDTO;
import com.lanf.comment.model.dto.ReplyCommentDTO;
import com.lanf.comment.model.enums.CommentStatusEnum;
import com.lanf.comment.model.enums.CommentTypeEnum;
import com.lanf.comment.model.query.CommentDocumentPageQuery;
import com.lanf.comment.model.query.CommentPageQuery;
import com.lanf.comment.model.query.CommentReplyQuery;
import com.lanf.comment.model.vo.CommentDocumentPageVO;
import com.lanf.comment.model.vo.CommentReplyVO;
import com.lanf.comment.model.vo.CommentStatsVO;
import com.lanf.comment.model.vo.CommentVO;
import com.lanf.comment.mq.constant.CommentMqTopicName;
import com.lanf.comment.mq.message.CommentLikeEventMessage;
import com.lanf.comment.repository.CommentLikeRepository;
import com.lanf.comment.repository.CommentRepository;
import com.lanf.comment.repository.CommentStatsRepository;
import com.lanf.comment.service.CommentService;
import com.lanf.comment.service.cache.CommentLikeCountRedisService;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.utils.IdUtils;
import com.lanf.constant.utils.UserContext;
import com.lanf.rocketmq.util.RocketMqClient;
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

import java.util.*;
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

    @Autowired
    private CommentLikeCountRedisService commentLikeCountRedisService;

    @Autowired
    private RocketMqClient rocketMqClient;

    @Override
    public Long publishComment(PublishCommentMessage dto) {

        Long userId = dto.getUserId();
        Long commentId = dto.getCommentId();
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
        Long goodsId = dto.getGoodsId();
        Boolean isLike = dto.getLike();
        log.info("评论点赞/取消点赞, userId={}, commentId={}, like={}", userId, commentId, isLike);

        if (isLike) {
            // 1. Redis Set 去重：检查用户是否已点赞
            boolean canLike = commentLikeCountRedisService.checkAndAddLike(goodsId,userId, commentId);
            if (!canLike) {
                log.warn("重复点赞, userId={}, commentId={}", userId, commentId);
                throw new BizException("您已点赞过该评论");
            }

            // 2. 写入 Redis Hash（原子递增，刷新过期时间 7 天）
            commentLikeCountRedisService.incrementLikeCount(goodsId, commentId);

        } else {
            // 1. 从 Redis Set 中移除点赞记录
            commentLikeCountRedisService.removeLikeFromSet(goodsId,userId, commentId);

            // 2. 写入 Redis Hash（原子递减，刷新过期时间 7 天）
            commentLikeCountRedisService.decrementLikeCount(goodsId, commentId);
        }

        // 3. 发送 MQ 顺序消息，下游消费更新 MongoDB 文档
        CommentLikeEventMessage eventMessage = new CommentLikeEventMessage();
        eventMessage.setGoodsId(goodsId);
        eventMessage.setCommentId(commentId);
        eventMessage.setLike(isLike);
        eventMessage.setUserId(userId);

        String tag = isLike ? CommentMqTopicName.COMMENT_LIKE_TAG : CommentMqTopicName.COMMENT_UNLIKE_TAG;
        rocketMqClient.sendOrderlyMessageWithTags(
                CommentMqTopicName.COMMENT_EVENT_TOPIC,
                tag,
                JsonUtils.toJsonString(eventMessage),
                commentId.toString());

        log.info("评论点赞操作完成, userId={}, commentId={}, like={}",
                userId, commentId, isLike);
    }

    @Override
    public PageResult<CommentVO> queryCommentPage(CommentPageQuery query) {
        // 1. 获取当前登录用户ID和商品ID
        Long currentUserId = UserContext.getUserId();
        Long goodsId = query.getGoodsId();
        log.info("分页查询商品评论, goodsId={}", goodsId);

        // 2. 构建分页条件：按创建时间降序
        Pageable pageable = PageRequest.of((int) (query.getPage() - 1), (int) query.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createTime"));

        // 3. 从 MongoDB 分页查询一级评论列表
        //    查询条件：指定商品、一级评论、正常状态、未删除
        Page<CommentDocument> page = commentRepository.findByGoodsIdAndCommentTypeAndStatusAndIsDeleted(
                goodsId, CommentTypeEnum.FIRST_LEVEL.getCode(),
                CommentStatusEnum.NORMAL.getCode(), 0, pageable);

        List<CommentDocument> documents = page.getContent();

        // 4. 提取所有评论ID，用于后续批量查询点赞数据
        List<Long> commentIds = documents.stream()
                .map(CommentDocument::getCommentId)
                .collect(Collectors.toList());

        // ==================== 批量获取点赞数（Redis → DB 回源）====================
        // 5.1 优先从 Redis Hash 批量获取点赞数
        //     Key: comment:like:count:{goodsId}
        //     Field: {commentId}, Value: likeCount
        Map<Long, Long> redisLikeCountMap = commentLikeCountRedisService.batchGetLikeCount(goodsId, commentIds);

        // 5.2 找出 Redis 中不存在的 commentId（缓存未命中）
        List<Long> missingCommentIds = commentIds.stream()
                .filter(id -> !redisLikeCountMap.containsKey(id))
                .collect(Collectors.toList());

        // 5.3 从 DB 批量补充 Redis 中不存在的点赞数（缓存穿透防护）
        Map<Long, Long> dbLikeCountMap = new HashMap<>();
        if (!missingCommentIds.isEmpty()) {
            // 使用 IN 查询批量获取，避免 N+1 问题
            List<CommentStatsDocument> statsList = commentStatsRepository.findByCommentIdIn(missingCommentIds);
            for (CommentStatsDocument stats : statsList) {
                dbLikeCountMap.put(stats.getCommentId(), stats.getLikeCount());
            }
        }

        // 5.4 合并 Redis 和 DB 的点赞数（Redis 优先，DB 补充）
        Map<Long, Long> finalLikeCountMap = new HashMap<>(redisLikeCountMap);
        finalLikeCountMap.putAll(dbLikeCountMap);

        // ==================== 批量判断当前用户是否已点赞（Redis Set）====================
        // 6. 优先从 Redis Set 批量判断用户点赞状态
        //    Key: comment:user:like:{goodsId}:{userId}
        //    Member: {commentId}
        Set<Long> likedCommentIds;
        if (currentUserId != null && !commentIds.isEmpty()) {
            likedCommentIds = commentLikeCountRedisService.batchCheckUserLiked(goodsId, currentUserId, commentIds);
        } else {
            likedCommentIds = new HashSet<>();
        }

        // 7. 组装 VO：将评论文档、点赞数、点赞状态合并为前端展示数据
        List<CommentVO> records = documents.stream()
                .map(doc -> convertToCommentVO(doc, currentUserId, finalLikeCountMap, likedCommentIds))
                .collect(Collectors.toList());

        // 8. 组装分页结果
        PageResult<CommentVO> result = new PageResult<>();
        result.setRecords(records);
        result.setTotal(page.getTotalElements());
        result.setSize(page.getSize());
        return result;
    }

    @Override
    public PageResult<CommentReplyVO> queryReplyPage(CommentReplyQuery query) {
        Long currentUserId = UserContext.getUserId();
        Long parentId = query.getParentId();
        log.info("分页查询评论回复, parentId={}", parentId);

        Pageable pageable = PageRequest.of((int) (query.getPage() - 1), (int) query.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createTime"));

        Page<CommentDocument> page = commentRepository.findByParentIdAndCommentTypeAndStatusAndIsDeleted(
                parentId, CommentTypeEnum.SECOND_LEVEL.getCode(),
                CommentStatusEnum.NORMAL.getCode(), 0, pageable);

        List<CommentDocument> documents = page.getContent();

        // 获取父评论的商品ID
        Long goodsId = null;
        if (!documents.isEmpty()) {
            goodsId = documents.get(0).getGoodsId();
        }

        // 批量获取 Redis 点赞数
        List<Long> commentIds = documents.stream()
                .map(CommentDocument::getCommentId)
                .collect(Collectors.toList());
        Map<Long, Long> redisLikeCountMap = commentLikeCountRedisService.batchGetLikeCount(goodsId, commentIds);


        // 从 DB 批量补充 Redis 中不存在的点赞数
        Map<Long, Long> dbLikeCountMap = new HashMap<>();
        List<Long> missingCommentIds = commentIds.stream()
                .filter(id -> !redisLikeCountMap.containsKey(id))
                .collect(Collectors.toList());
        if (!redisLikeCountMap.isEmpty()) {
            List<CommentStatsDocument> statsList = commentStatsRepository.findByCommentIdIn(missingCommentIds);
            for (CommentStatsDocument stats : statsList) {
                dbLikeCountMap.put(stats.getCommentId(), stats.getLikeCount());
            }
        }
        // 合并两个 map
        Map<Long, Long> finalLikeCountMap = new HashMap<>(redisLikeCountMap);
        finalLikeCountMap.putAll(dbLikeCountMap);

        // 批量判断当前用户是否点赞过这些评论
        Set<Long> likedCommentIds = new HashSet<>();
        if (currentUserId != null && goodsId != null && !commentIds.isEmpty()) {
            likedCommentIds = commentLikeCountRedisService.batchCheckUserLiked(goodsId, currentUserId, commentIds);
        }

        List<CommentReplyVO> records = documents.stream()
                .map(doc -> convertToReplyVO(doc, currentUserId, finalLikeCountMap, null))
                .collect(Collectors.toList());

        PageResult<CommentReplyVO> result = new PageResult<>();
        result.setRecords(records);
        result.setTotal(page.getTotalElements());
        result.setSize(page.getSize());
        return result;
    }

    @Override
    public CommentStatsVO getCommentStats(Long commentId) {
        // 先从 CommentDocument 中获取商品ID
        CommentDocument comment = mongoTemplate.findOne(
                Query.query(Criteria.where("commentId").is(commentId)),
                CommentDocument.class);

        if (comment != null && comment.getGoodsId() != null) {
            // 先从 Redis 查询
            Long redisLikeCount = commentLikeCountRedisService.getLikeCount(comment.getGoodsId(), commentId);
            if (redisLikeCount != null) {
                CommentStatsVO vo = new CommentStatsVO();
                vo.setCommentId(commentId);
                vo.setLikeCount(redisLikeCount);
                // 回复数从 DB 查询
                CommentStatsDocument stats = commentStatsRepository.findByCommentId(commentId);
                vo.setReplyCount(stats != null ? stats.getReplyCount() : 0L);
                return vo;
            }
        }

        // Redis 未命中，从 DB 查询
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

    @Override
    public PageResult<CommentDocumentPageVO> commentDocumentPageQuery(CommentDocumentPageQuery query) {
        log.info("分页查询评论文档列表, query={}", query);

        // 1. 构建查询条件
        Criteria criteria = new Criteria();
        if (query.getGoodsId() != null) {
            criteria.and("goodsId").is(query.getGoodsId());
        }
        if (query.getUserId() != null) {
            criteria.and("userId").is(query.getUserId());
        }
        if (query.getStatus() != null) {
            criteria.and("status").is(query.getStatus());
        }
        if (query.getCommentType() != null) {
            criteria.and("commentType").is(query.getCommentType());
        }
        if (query.getParentId() != null) {
            criteria.and("parentId").is(query.getParentId());
        }
        if (query.getContent() != null && !query.getContent().trim().isEmpty()) {
            criteria.and("content").regex(".*" + query.getContent().trim() + ".*", "i");
        }

        // 2. 构建分页条件
        Pageable pageable = PageRequest.of((int) (query.getPage() - 1), (int) query.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createTime"));

        // 3. 执行分页查询
        Query mongoQuery = new Query(criteria).with(pageable);
        List<CommentDocument> documents = mongoTemplate.find(mongoQuery, CommentDocument.class);
        long total = mongoTemplate.count(new Query(criteria), CommentDocument.class);

        // 4. 提取所有评论ID，批量查询统计数据（点赞数、回复数）
        List<Long> commentIds = documents.stream()
                .map(CommentDocument::getCommentId)
                .collect(Collectors.toList());

        Map<Long, CommentStatsDocument> statsMap = new HashMap<>();
        if (!commentIds.isEmpty()) {
            List<CommentStatsDocument> statsList = commentStatsRepository.findByCommentIdIn(commentIds);
            for (CommentStatsDocument stats : statsList) {
                statsMap.put(stats.getCommentId(), stats);
            }
        }

        // 5. 组装VO
        List<CommentDocumentPageVO> records = documents.stream()
                .map(doc -> {
                    CommentDocumentPageVO vo = new CommentDocumentPageVO();
                    vo.setCommentId(doc.getCommentId());
                    vo.setGoodsId(doc.getGoodsId());
                    vo.setOrderId(doc.getOrderId());
                    vo.setSkuCode(doc.getSkuCode());
                    vo.setUserId(doc.getUserId());
                    vo.setUserName(doc.getUserName());
                    vo.setUserAvatar(doc.getUserAvatar());
                    vo.setContent(doc.getContent());
                    vo.setImages(doc.getImages());
                    vo.setRating(doc.getRating());
                    vo.setCommentType(doc.getCommentType());
                    vo.setParentId(doc.getParentId());
                    vo.setReplyToUserId(doc.getReplyToUserId());
                    vo.setReplyToUserName(doc.getReplyToUserName());
                    vo.setStatus(doc.getStatus());
                    vo.setTopFlag(doc.getTopFlag());
                    vo.setCreateTime(doc.getCreateTime());
                    vo.setUpdateTime(doc.getUpdateTime());
                    vo.setIsDeleted(doc.getIsDeleted());

                    // 关联点赞数和回复数
                    CommentStatsDocument stats = statsMap.get(doc.getCommentId());
                    if (stats != null) {
                        vo.setLikeCount(stats.getLikeCount());
                        vo.setReplyCount(stats.getReplyCount());
                    } else {
                        vo.setLikeCount(0L);
                        vo.setReplyCount(0L);
                    }

                    return vo;
                })
                .collect(Collectors.toList());

        // 6. 组装分页结果
        PageResult<CommentDocumentPageVO> result = new PageResult<>();
        result.setRecords(records);
        result.setTotal(total);
        result.setSize(pageable.getPageSize());
        return result;
    }

    // ==================== 私有转换方法 ====================

    private CommentVO convertToCommentVO(CommentDocument doc, Long currentUserId,
                                         Map<Long, Long> likeCountMap, Set<Long> likedCommentIds) {
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

        // 从 map 中获取点赞数
        Long likeCount = likeCountMap.get(doc.getCommentId());
        vo.setLikeCount(likeCount != null ? likeCount : 0L);

        // 当前用户是否已点赞
        vo.setLikedByCurrentUser(likedCommentIds != null && likedCommentIds.contains(doc.getCommentId()));

        return vo;
    }

    private CommentReplyVO convertToReplyVO(CommentDocument doc, Long currentUserId,
                                            Map<Long, Long> likeCountMap, Set<Long> likedCommentIds) {
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

        // 从 map 中获取点赞数
        Long likeCount = likeCountMap.get(doc.getCommentId());
        vo.setLikeCount(likeCount != null ? likeCount : 0L);

        // 当前用户是否已点赞
        vo.setLikedByCurrentUser(likedCommentIds != null && likedCommentIds.contains(doc.getCommentId()));

        return vo;
    }
}