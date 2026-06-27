package com.lanf.comment.mq.listener;

import com.lanf.api.order.mq.constant.OrderClientTopicName;
import com.lanf.api.order.mq.message.PublishCommentMessage;
import com.lanf.comment.mq.constant.CommentMqGroupName;
import com.lanf.comment.service.CommentService;
import com.lanf.common.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(
        consumerGroup = CommentMqGroupName.PUBLISH_COMMENT_GROUP,
        topic = OrderClientTopicName.PUBLISH_COMMENT_TOPIC
)
public class PublishCommentListener implements RocketMQListener<PublishCommentMessage> {

    @Autowired
    private CommentService commentService;
    @Override
    public void onMessage(PublishCommentMessage publishCommentMessage) {

        log.info("发布商品评论:{}", JsonUtils.toJsonString(publishCommentMessage));

        commentService.publishComment(publishCommentMessage);

    }
}
