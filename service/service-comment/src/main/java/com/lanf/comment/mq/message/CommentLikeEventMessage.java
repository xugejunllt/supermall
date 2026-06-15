package com.lanf.comment.mq.message;

import lombok.Data;

import java.io.Serializable;

/**
 * 评论点赞事件消息
 * <p>
 * 用于 MQ 顺序消费，保证同一评论的点赞操作按序处理
 *
 * @author lanf
 */
@Data
public class CommentLikeEventMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品ID
     */
    private Long goodsId;

    /**
     * 评论ID
     */
    private Long commentId;

    /**
     * 操作类型：true-点赞，false-取消点赞
     */
    private Boolean like;



    /**
     * 用户ID
     */
    private Long userId;

}
