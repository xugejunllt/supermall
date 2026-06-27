package com.lanf.order.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 发布评论 DTO
 *
 */
@Data
public class PublishCommentDTO {

    /**
     * 商品ID
     */
    @NotNull(message = "商品ID不能为空")
    private Long goodsId;

    /**
     * 订单ID
     */
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    /**
     * SKU编码（可选）
     */
    private String skuCode;

    /**
     * 评论内容
     */
    @NotBlank(message = "评论内容不能为空")
    private String content;

    /**
     * 评论图片列表（可选）
     */
    private List<String> images;

    /**
     * 评分（1-5星，可选）
     */
    private Integer rating;
}
