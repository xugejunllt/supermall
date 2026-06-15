package com.lanf.comment.mq.listener;

import com.lanf.comment.model.document.CommentStatsDocument;
import com.lanf.comment.mq.constant.CommentMqGroupName;
import com.lanf.comment.mq.constant.CommentMqTopicName;
import com.lanf.comment.mq.message.CommentLikeEventMessage;
import com.lanf.common.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
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
public class CommentLikeEventListener implements RocketMQListener<String> {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void onMessage(String messageJson) {
        try {
            CommentLikeEventMessage message = JsonUtils.toObject(messageJson, CommentLikeEventMessage.class);
            if (message == null || message.getCommentId() == null) {
                log.warn("点赞事件消息解析失败或数据不完整, message={}", messageJson);
                return;
            }

            Long commentId = message.getCommentId();
            Long goodsId = message.getGoodsId();
            Boolean isLike = message.getLike();
            long delta = Boolean.TRUE.equals(isLike) ? 1 : -1;
            Date now = new Date();

            log.info("收到点赞事件, commentId={}, like={}, delta={}", commentId, isLike, delta);

            // 查询统计文档是否存在
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
                mongoTemplate.save(newStats);
                log.info("点赞事件处理完成, commentId={}, 新增统计文档, likeCount={}", commentId, newStats.getLikeCount());
            } else {
                // 存在：累加更新点赞数
                long newLikeCount = Math.max(0, stats.getLikeCount() + delta);
                stats.setLikeCount(newLikeCount);
                stats.setUpdateTime(now);
                mongoTemplate.save(stats);
                log.info("点赞事件处理完成, commentId={}, 累加更新, likeCount={}", commentId, newLikeCount);
            }

        } catch (Exception e) {
            log.error("点赞事件处理异常, message={}", messageJson, e);
            // 顺序消息消费异常会重试，不吞异常
            throw new RuntimeException("点赞事件处理失败", e);
        }
    }
}