package com.lanf.order.service;

import com.lanf.order.model.dto.PlaceOrderDTO;
import com.lanf.order.model.vo.PlaceOrderVO;

public interface OrderManagerService {

    /**
     * 立即下单
     *
     */
    PlaceOrderVO placeOrder(PlaceOrderDTO orderDTO);
}
