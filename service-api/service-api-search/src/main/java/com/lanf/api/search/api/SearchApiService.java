package com.lanf.api.search.api;


import com.lanf.api.search.model.query.GoodsDocumentPageQuery;
import com.lanf.api.search.model.query.OrderSearchQuery;
import com.lanf.api.search.model.vo.GoodsDocumentPageVO;
import com.lanf.api.search.model.vo.OrderSearchVO;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@Component
@FeignClient(name = "service-search") //调用的服务名称
public interface SearchApiService {




    @PostMapping("/searchOrders")
    public Result<PageResult<OrderSearchVO>> searchOrders(@RequestBody  OrderSearchQuery query);

    @PostMapping("/search/admin/search/searchOrders")
    public Result<PageResult<OrderSearchVO>> searchOrdersForAdmin(@RequestBody  OrderSearchQuery query);

    /**
     * 分页查询商品文档列表
     */
    @GetMapping("/search/admin/search/goodsDocumentPageQuery")
    Result<PageResult<GoodsDocumentPageVO>> goodsDocumentPageQuery(@SpringQueryMap GoodsDocumentPageQuery query);
}
