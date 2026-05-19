package com.lanf.api.search.api;


import com.lanf.api.search.model.query.OrderSearchQuery;
import com.lanf.api.search.model.vo.OrderSearchVO;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@Component
@FeignClient(name = "service-search",url = "localhost:9014") //调用的服务名称
public interface SearchApiService {




    @PostMapping("/searchOrders")
    public Result<PageResult<OrderSearchVO>> searchOrders(@RequestBody  OrderSearchQuery query);

    @PostMapping("/search/admin/search/searchOrders")
    public Result<PageResult<OrderSearchVO>> searchOrdersForAdmin(@RequestBody  OrderSearchQuery query);
}
