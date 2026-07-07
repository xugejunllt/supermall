package com.lanf.comment.mq.listener;

import com.lanf.comment.model.document.CommentLikeDocument;
import com.lanf.comment.model.document.CommentStatsDocument;
import com.lanf.comment.mq.constant.CommentMqGroupName;
import com.lanf.comment.mq.constant.CommentMqTopicName;
import com.lanf.comment.mq.message.CommentLikeEventMessage;
import com.lanf.comment.repository.CommentLikeRepository;
import com.lanf.rocketmq.annotation.MqRetryConsume;
import com.lanf.rocketmq.exception.MessageRetryConsumeException;
import com.mongodb.client.result.UpdateResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 评论点赞事件 MQ 监听器
 * <p>
 * 顺序消费点赞事件，更新 MongoDB 文档中的点赞数
 *
 * @author lanf
 */
@Slf4j
@Component
@RocketMQMessageListener(
        consumerGroup = CommentMqGroupName.COMMENT_LIKE_EVENT_GROUP,
        topic = CommentMqTopicName.COMMENT_EVENT_TOPIC,
        selectorExpression = CommentMqTopicName.COMMENT_LIKE_TAG + "||" + CommentMqTopicName.COMMENT_UNLIKE_TAG,
        consumeMode = ConsumeMode.ORDERLY
)
public class CommentLikeEventListener implements RocketMQListener<CommentLikeEventMessage> {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private CommentLikeRepository commentLikeRepository;
    @MqRetryConsume(messageId = "#message.messageId")
    @Override
    public void onMessage(CommentLikeEventMessage message) {

        try {

            log.info("收到点赞事件, message={}", message);

            Long commentId = message.getCommentId();
            Long goodsId = message.getGoodsId();
            Long userId = message.getUserId();
            Boolean isLike = message.getLike();
            long delta = Boolean.TRUE.equals(isLike) ? 1 : -1;
            Date now = new Date();

            log.info("收到点赞事件, commentId={}, like={}, delta={}", commentId, isLike, delta);

            // 1. 幂等检查：查询用户点赞记录是否已存在
            CommentLikeDocument likeRecord = commentLikeRepository.findByUserIdAndCommentId(userId, commentId);
            if (isLike) {
                if (likeRecord != null) {
                    log.info("用户已有点赞记录，幂等返回, userId={}, commentId={}", userId, commentId);
                    return;
                }
                // 插入点赞记录
                CommentLikeDocument newRecord = new CommentLikeDocument();
                newRecord.setUserId(userId);
                newRecord.setCommentId(commentId);
                newRecord.setGoodsId(goodsId);
                newRecord.setCreateTime(now);
                try {
                    commentLikeRepository.save(newRecord);
                } catch ( DuplicateKeyException  e) {
                    log.warn("重复点赞");
                    return;
                }
                log.info("用户点赞记录已插入, userId={}, commentId={}", userId, commentId);
            } else {
                // 取消点赞：删除点赞记录
                if (likeRecord != null) {
                    commentLikeRepository.deleteByUserIdAndCommentId(userId, commentId);
                    log.info("用户点赞记录已删除, userId={}, commentId={}", userId, commentId);
                }
            }

            // 2. 查询统计文档是否存在
            CommentStatsDocument stats = mongoTemplate.findOne(
                    Query.query(Criteria.where("commentId").is(commentId)),
                    CommentStatsDocument.class);

            if (stats == null) {
                // 不存在：插入新文档，点赞数默认为 delta（点赞=1，取消点赞=0）
                CommentStatsDocument newStats = new CommentStatsDocument();
                newStats.setCommentId(commentId);
                newStats.setGoodsId(goodsId);
                newStats.setLikeCount(Math.max(0L, delta));
                newStats.setReplyCount(0L);
                newStats.setDislikeCount(0L);
                newStats.setReportCount(0);
                newStats.setCreateTime(now);
                newStats.setUpdateTime(now);
                newStats.setVersion(1L);
                mongoTemplate.save(newStats);
                log.info("点赞事件处理完成, commentId={}, 新增统计文档, likeCount={}", commentId, newStats.getLikeCount());
            } else {
                // 存在：使用乐观锁更新
                optimisticUpdateLikeCount(commentId, delta, now);
            }

        } catch (Exception e) {
            log.error("点赞事件处理异常,", e);
            // 顺序消息消费异常会重试，不吞异常
            //throw new MessageRetryConsumeException("点赞事件处理失败");
        }

    }

    /**
     * 乐观锁更新点赞数
     * <p>
     * 使用 version 字段作为乐观锁条件，更新失败时自动重试，
     * 避免并发场景下数据覆盖问题。
     *
     * @param commentId 评论ID
     * @param delta     增量（+1 或 -1）
     * @param now       当前时间
     */
    private void optimisticUpdateLikeCount(Long commentId, long delta, Date now) {

        // 查询当前文档和版本号
        CommentStatsDocument stats = mongoTemplate.findOne(
                Query.query(Criteria.where("commentId").is(commentId)),
                CommentStatsDocument.class);

        if (stats == null) {
            throw new RuntimeException("文档不存在, commentId=" + commentId);
        }

        long currentVersion = stats.getVersion();
        long newLikeCount = Math.max(0, stats.getLikeCount() + delta);

        // 乐观锁更新：version 作为条件
        Update update = new Update()
                .set("likeCount", newLikeCount)
                .set("updateTime", now)
                .inc("version", 1);

        Query query = Query.query(
                Criteria.where("commentId").is(commentId)
                        .and("version").is(currentVersion));

        UpdateResult result =
                mongoTemplate.updateFirst(query, update, CommentStatsDocument.class);

        if (result.getModifiedCount() > 0) {
            log.info("点赞事件处理完成, commentId={}, likeCount={}, version={}",
                    commentId, newLikeCount, currentVersion + 1);
            return;
        }


        throw new MessageRetryConsumeException("更新点赞数失败 commentId=" + commentId);
    }
}