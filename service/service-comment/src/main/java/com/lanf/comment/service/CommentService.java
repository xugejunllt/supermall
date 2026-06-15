package com.lanf.comment.service;

import com.lanf.comment.model.dto.LikeCommentDTO;
import com.lanf.comment.model.dto.PublishCommentDTO;
import com.lanf.comment.model.dto.ReplyCommentDTO;
import com.lanf.comment.model.query.CommentPageQuery;
import com.lanf.comment.model.query.CommentReplyQuery;
import com.lanf.comment.model.vo.CommentStatsVO;
import com.lanf.comment.model.vo.CommentVO;
import com.lanf.comment.model.vo.CommentReplyVO;
import com.lanf.constant.model.vo.PageResult;

import java.util.List;

/**
 * 评论 Service 接口
 *
 * @author lanf
 */
public interface CommentService {

    /**
     * 发布评论（一级评论）
     */
    Long publishComment(PublishCommentDTO dto);

    /**
     * 回复评论（二级回复）
     */
    Long replyComment(ReplyCommentDTO dto);

    /**
     * 评论点赞/取消点赞
     */
    void likeComment(LikeCommentDTO dto);

    /**
     * 分页查询商品评论列表（一级评论）
     */
    PageResult<CommentVO> queryCommentPage(CommentPageQuery query);

    /**
     * 查询评论回复列表（二级回复）
     */
    PageResult<CommentReplyVO> queryReplyPage(CommentReplyQuery query);

    /**
     * 获取评论统计数据
     */
    CommentStatsVO getCommentStats(Long commentId);

    /**
     * 统计商品的评论数量
     */
    Long countGoodsComment(Long goodsId);
}
