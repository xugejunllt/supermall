package com.lanf.comment.controller.admin;

import com.lanf.comment.model.query.CommentDocumentPageQuery;
import com.lanf.comment.model.vo.CommentDocumentPageVO;
import com.lanf.comment.service.CommentService;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 评论管理后台控制器
 *
 * @author lanf
 */
@Slf4j
@RestController
@RequestMapping("/admin/comment")
public class CommentForAdminController {

    @Autowired
    private CommentService commentService;

    /**
     * 分页查询评论文档列表（关联点赞数）
     */
    @GetMapping("/commentDocumentPageQuery")
    public Result<PageResult<CommentDocumentPageVO>> commentDocumentPageQuery(@Validated CommentDocumentPageQuery query) {
        log.info("后台分页查询评论文档列表, query={}", query);
        return Result.ok(commentService.commentDocumentPageQuery(query));
    }

}
