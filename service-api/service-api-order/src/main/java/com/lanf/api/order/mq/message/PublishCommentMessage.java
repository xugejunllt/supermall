package com.lanf.api.order.mq.message;

import com.lanf.constant.mq.base.BaseMessage;
import lombok.Data;

import java.util.List;

@Data
public class PublishCommentMessage extends BaseMessage {


    private Long userId;
    /**
     * 商品ID
     */
    private Long goodsId;

    /**
     * 订单ID
     */
    private Long orderId;

    private String skuCode;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 评论图片列表（可选）
     */
    private List<String> images;

    /**
     * 评分（1-5星，可选）
     */
    private Integer rating;

    private Long commentId;

}
