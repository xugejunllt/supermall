package com.lanf.comment.model.query;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 评论分页查询条件
 *
 * @author lanf
 */
@Data
public class CommentPageQuery {

    /**
     * 商品ID
     */
    @NotNull(message = "商品ID不能为空")
    private Long goodsId;

    /**
     * 页码
     */
    private Integer page = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;
}
