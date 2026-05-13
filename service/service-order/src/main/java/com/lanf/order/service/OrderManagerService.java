package com.lanf.order.service;

import com.lanf.api.goods.model.dto.ValidateCartDTO;
import com.lanf.order.model.dto.CalculateOrderAmountDTO;
import com.lanf.order.model.dto.CancelOrderDTO;
import com.lanf.order.model.dto.PlaceOrderDTO;
import com.lanf.order.model.dto.SubmitCartDTO;
import com.lanf.order.model.vo.CalculateOrderAmountVO;
import com.lanf.order.model.vo.PlaceOrderVO;
import com.lanf.order.model.vo.SubmitCartVO;
import com.lanf.order.model.vo.ValidateCartVO;

public interface OrderManagerService {


    /**
     *  立即下单前 计算订单金额计算
     *
     *
     */
    CalculateOrderAmountVO calculateOrderAmount(CalculateOrderAmountDTO dto);

    /**
     * 立即下单
     *
     */
    PlaceOrderVO placeOrder(PlaceOrderDTO orderDTO);

    /**
     * 校验购物车
     *
     *
     */
    ValidateCartVO validateCart(ValidateCartDTO dto);

    /**
     * 提交购物车
     *
     *
     */
    SubmitCartVO submitCart(SubmitCartDTO dto);

    /**
     * 取消订单
     *
     */
    void cancelOrder(CancelOrderDTO dto);


}
