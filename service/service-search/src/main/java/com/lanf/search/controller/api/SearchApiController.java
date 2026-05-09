package com.lanf.search.controller.api;

import com.alibaba.nacos.api.model.v2.Result;
import com.lanf.mybatis.base.PageResult;
import com.lanf.search.model.query.HomePageQuery;
import com.lanf.search.model.query.OrderSearchQuery;
import com.lanf.search.model.vo.HomePageVO;
import com.lanf.search.model.vo.OrderSearchVO;
import com.lanf.search.service.IGoodsDocumentService;
import com.lanf.search.service.IOrderSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/goods")
public class SearchApiController {

    @Autowired
    private IGoodsDocumentService goodsDocumentService;

    @Autowired
    private IOrderSearchService orderSearchService;

    /**
     * 首页商品分页查询
     * @param query 查询参数
     * @return 分页结果
     */
    @GetMapping("/pageHomePage")
    public Result<PageResult<HomePageVO>> pageHomePage( HomePageQuery query) {

        log.info("首页查询{}",query);
        PageResult<HomePageVO> result = goodsDocumentService.pageHomePage(query);
        return Result.success(result);
    }

    /**
     * 订单搜索
     *
     *
     */
    @PostMapping("/searchOrders")
    public Result<PageResult<OrderSearchVO>> searchOrders(@RequestBody  OrderSearchQuery query) {

        log.info("订单搜索{}",query);
        return Result.success(orderSearchService.searchOrders(query));
    }

}
