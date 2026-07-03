package com.lanf.comment.model.query;

import com.lanf.constant.model.query.PageQuery;
import lombok.Data;

import java.io.Serializable;

/**
 * 评论文档分页查询条件
 *
 * @author lanf
 */
@Data
public class CommentDocumentPageQuery extends PageQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品ID
     */
    private Long goodsId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 评论状态：1-正常，2-隐藏，3-已删除
     */
    private Integer status;

    /**
     * 评论类型：1-一级评论，2-二级回复
     */
    private Integer commentType;

    /**
     * 评论内容（模糊查询）
     */
    private String content;

    /**
     * 父评论ID
     */
    private Long parentId;

}
