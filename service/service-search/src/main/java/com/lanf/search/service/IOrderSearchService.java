package com.lanf.search.service;

import com.lanf.constant.model.vo.PageResult;
import com.lanf.search.model.query.OrderSearchQuery;
import com.lanf.search.model.vo.OrderSearchVO;

public interface IOrderSearchService {

    PageResult<OrderSearchVO> searchOrders(OrderSearchQuery query);
}
