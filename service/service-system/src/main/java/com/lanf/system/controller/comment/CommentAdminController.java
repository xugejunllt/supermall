package com.lanf.system.controller.comment;

import com.lanf.comment.api.CommentApiService;
import com.lanf.comment.model.query.CommentDocumentPageQuery;
import com.lanf.comment.model.vo.CommentDocumentPageVO;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/comment")
public class CommentAdminController {

    @Autowired
    private CommentApiService commentApiService;

    /**
     * 分页查询评论文档列表（关联点赞数）
     */
    @GetMapping("/commentDocumentPageQuery")
    public Result<PageResult<CommentDocumentPageVO>> commentDocumentPageQuery(@Validated CommentDocumentPageQuery query) {
        log.info("后台分页查询评论文档列表, query={}", query);
        return commentApiService.commentDocumentPageQuery(query);
    }

}
