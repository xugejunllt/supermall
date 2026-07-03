package com.lanf.comment.api;

import com.lanf.comment.model.query.CommentDocumentPageQuery;
import com.lanf.comment.model.vo.CommentDocumentPageVO;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.cloud.openfeign.SpringQueryMap;

@Component
@FeignClient(name = "service-comment")
public interface CommentApiService {

    /**
     * 分页查询评论文档列表（关联点赞数）
     */
    @GetMapping("/comment/admin/comment/commentDocumentPageQuery")
    Result<PageResult<CommentDocumentPageVO>> commentDocumentPageQuery(@SpringQueryMap CommentDocumentPageQuery query);

}
