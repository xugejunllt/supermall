package com.lanf.comment.mq.constant;

/**
 * 评论模块 MQ Topic 常量
 *
 * @author lanf
 */
public class CommentMqTopicName {

    /**
     * 评论事件 Topic
     */
    public static final String COMMENT_EVENT_TOPIC = "COMMENT_EVENT_TOPIC";

    /**
     * 点赞 Tag
     */
    public static final String COMMENT_LIKE_TAG = "LIKE";

    /**
     * 取消点赞 Tag
     */
    public static final String COMMENT_UNLIKE_TAG = "UNLIKE";
}
