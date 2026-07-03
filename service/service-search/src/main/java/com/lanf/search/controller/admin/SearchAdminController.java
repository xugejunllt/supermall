package com.lanf.search.controller.admin;

import com.lanf.api.search.model.query.GoodsDocumentPageQuery;
import com.lanf.api.search.model.query.OrderSearchQuery;
import com.lanf.api.search.model.vo.GoodsDocumentPageVO;
import com.lanf.api.search.model.vo.OrderSearchVO;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import com.lanf.search.service.IGoodsDocumentService;
import com.lanf.search.service.IOrderSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/admin/search")
public class SearchAdminController {

    @Autowired
    private IGoodsDocumentService goodsDocumentService;

    @Autowired
    private IOrderSearchService orderSearchService;

    /**
     * 订单搜索
     *
     *
     */
    @PostMapping("/searchOrders")
    public Result<PageResult<OrderSearchVO>> searchOrdersForAdmin(@RequestBody OrderSearchQuery query) {

        log.info("订单搜索{}",query);
        return Result.ok(orderSearchService.searchOrders(query));
    }

    /**
     * 分页查询商品文档列表
     */
    @GetMapping("/goodsDocumentPageQuery")
    public Result<PageResult<GoodsDocumentPageVO>> goodsDocumentPageQuery(GoodsDocumentPageQuery query) {
        log.info("后台分页查询商品文档列表, query={}", query);
        return Result.ok(goodsDocumentService.goodsDocumentPageQuery(query));
    }

}
