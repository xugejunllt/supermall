package com.lanf.comment.model.query;

import com.lanf.constant.model.query.PageQuery;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 评论分页查询条件
 *
 * @author lanf
 */
@Data
public class CommentPageQuery extends PageQuery {

    /**
     * 商品ID
     */
    @NotNull(message = "商品ID不能为空")
    private Long goodsId;


}
