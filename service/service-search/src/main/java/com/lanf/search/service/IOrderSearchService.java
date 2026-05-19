package com.lanf.search.service;

import com.lanf.api.search.model.query.OrderSearchQuery;
import com.lanf.api.search.model.vo.OrderSearchVO;
import com.lanf.constant.model.vo.PageResult;

public interface IOrderSearchService {

    PageResult<OrderSearchVO> searchOrders(OrderSearchQuery query);
}
