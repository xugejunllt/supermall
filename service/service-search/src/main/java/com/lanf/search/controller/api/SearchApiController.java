package com.lanf.search.controller.api;

import com.alibaba.nacos.api.model.v2.Result;
import com.lanf.api.search.model.query.OrderSearchQuery;
import com.lanf.api.search.model.vo.OrderSearchVO;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.search.service.IGoodsDocumentService;
import com.lanf.search.service.IOrderSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/goods")
public class SearchApiController {

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
    public Result<PageResult<OrderSearchVO>> searchOrders(@RequestBody OrderSearchQuery query) {

        log.info("订单搜索{}",query);
        return Result.success(orderSearchService.searchOrders(query));
    }



}
