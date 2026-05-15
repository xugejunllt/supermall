package com.lanf.search.controller.app;

import com.alibaba.nacos.api.model.v2.Result;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.search.model.query.HomePageQuery;
import com.lanf.search.model.query.SuggestQuery;
import com.lanf.search.model.vo.HomePageVO;
import com.lanf.search.model.vo.SuggestVO;
import com.lanf.search.service.IGoodsDocumentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/app/goods")
public class SearchController {

    @Autowired
    private IGoodsDocumentService goodsDocumentService;


    /**
     * 首页商品分页查询
     * @param query 查询参数
     * @return 分页结果
     */
    @GetMapping("/pageHomePage")
    public Result<PageResult<HomePageVO>> pageHomePage(HomePageQuery query) {

        log.info("首页查询{}",query);
        PageResult<HomePageVO> result = goodsDocumentService.pageHomePage(query);
        return Result.success(result);
    }


    /**
     * 查询词建议 - 输入前缀返回建议词列表
     * @param query 建议查询参数
     * @return 建议词列表
     */
    @GetMapping("/suggestions")
    public Result<List<SuggestVO>> getSuggestions( SuggestQuery query) {
        log.info("查询词建议{}", query);
        List<SuggestVO> suggestions = goodsDocumentService.getSuggestions(query);
        return Result.success(suggestions);
    }

}
