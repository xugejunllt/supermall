package com.lanf.system.controller.search;

import com.lanf.api.search.api.SearchApiService;
import com.lanf.api.search.model.query.GoodsDocumentPageQuery;
import com.lanf.api.search.model.query.OrderSearchQuery;
import com.lanf.api.search.model.vo.GoodsDocumentPageVO;
import com.lanf.api.search.model.vo.OrderSearchVO;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.lanf.constant.result.Result;

@Slf4j
@RestController
@RequestMapping("/search")
public class SearchAdminController {

    @Autowired
    private SearchApiService searchApiService;
    /**
     * 订单搜索
     *
     *
     */
    @GetMapping("/searchOrders")
    public Result<PageResult<OrderSearchVO>> searchOrdersForAdmin( OrderSearchQuery query) {

        log.info("订单搜索{}",query);
        query.setTenantId(UserContext.getTenantId());
        return searchApiService.searchOrdersForAdmin( query);
    }

    /**
     * 分页查询商品文档列表
     */
    @GetMapping("/goodsDocumentPageQuery")
    public Result<PageResult<GoodsDocumentPageVO>> goodsDocumentPageQuery(GoodsDocumentPageQuery query) {
        log.info("后台分页查询商品文档列表, query={}", query);
        query.setTenantId(UserContext.getTenantId());
        return searchApiService.goodsDocumentPageQuery(query);
    }

}
