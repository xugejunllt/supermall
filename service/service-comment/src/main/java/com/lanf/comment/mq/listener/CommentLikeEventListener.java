package com.lanf.comment.mq.listener;

import com.lanf.comment.model.document.CommentDocument;
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
            Long newLikeCount = message.getNewLikeCount();
            Date now = new Date();

            log.info("收到点赞事件, commentId={}, like={}, newLikeCount={}",
                    commentId, message.getLike(), newLikeCount);


            // 更新统计文档的点赞数
            mongoTemplate.updateFirst(
                    Query.query(Criteria.where("commentId").is(commentId)),
                    new Update().set("likeCount", newLikeCount).set("updateTime", now),
                    CommentStatsDocument.class);

            log.info("点赞事件处理完成, commentId={}, newLikeCount={}", commentId, newLikeCount);

        } catch (Exception e) {
            log.error("点赞事件处理异常, message={}", messageJson, e);
            // 顺序消息消费异常会重试，不吞异常
            throw new RuntimeException("点赞事件处理失败", e);
        }
    }
}
