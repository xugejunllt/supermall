package com.lanf.comment.controller.app;

import com.lanf.comment.model.dto.LikeCommentDTO;
import com.lanf.comment.model.dto.PublishCommentDTO;
import com.lanf.comment.model.dto.ReplyCommentDTO;
import com.lanf.comment.model.query.CommentPageQuery;
import com.lanf.comment.model.query.CommentReplyQuery;
import com.lanf.comment.model.vo.CommentReplyVO;
import com.lanf.comment.model.vo.CommentStatsVO;
import com.lanf.comment.model.vo.CommentVO;
import com.lanf.comment.service.CommentService;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 评论 App 端 Controller
 *
 * @author lanf
 */
@Slf4j
@RestController
@RequestMapping("/app/comment")
public class CommentAppController {

    @Autowired
    private CommentService commentService;

    /**
     * 发布评论（一级评论）
     */
    @PostMapping("/publish")
    public Result<Long> publishComment(@Validated @RequestBody PublishCommentDTO dto) {
        log.info("发布评论:dto={}", dto);
        Long commentId = commentService.publishComment(dto);
        return Result.ok(commentId);
    }

    /**
     * 回复评论（二级回复）
     */
    @PostMapping("/reply")
    public Result<Long> replyComment(@Validated @RequestBody ReplyCommentDTO dto) {
        log.info("回复评论:dto={}", dto);
        Long commentId = commentService.replyComment(dto);
        return Result.ok(commentId);
    }

    /**
     * 评论点赞/取消点赞
     */
    @PostMapping("/like")
    public Result<Void> likeComment(@Validated @RequestBody LikeCommentDTO dto) {
        log.info("评论点赞/取消点赞:dto={}", dto);
        commentService.likeComment(dto);
        return Result.ok();
    }

    /**
     * 分页查询商品评论列表（一级评论）
     */
    @GetMapping("/page")
    public Result<PageResult<CommentVO>> queryCommentPage(@Validated CommentPageQuery query) {
        log.info("分页查询商品评论:query={}", query);
        return Result.ok(commentService.queryCommentPage(query));
    }

    /**
     * 查询评论回复列表（二级回复）
     */
    @GetMapping("/replyPage")
    public Result<PageResult<CommentReplyVO>> queryReplyPage(@Validated CommentReplyQuery query) {
        log.info("分页查询评论回复:query={}", query);
        return Result.ok(commentService.queryReplyPage(query));
    }

    /**
     * 获取评论统计数据
     */
    @GetMapping("/stats/{commentId}")
    public Result<CommentStatsVO> getCommentStats(@PathVariable("commentId") Long commentId) {
        log.info("获取评论统计数据:commentId={}", commentId);
        return Result.ok(commentService.getCommentStats(commentId));
    }

    /**
     * 统计商品的评论数量
     */
    @GetMapping("/count/{goodsId}")
    public Result<Long> countGoodsComment(@PathVariable("goodsId") Long goodsId) {
        log.info("统计商品评论数量:goodsId={}", goodsId);
        return Result.ok(commentService.countGoodsComment(goodsId));
    }
}
