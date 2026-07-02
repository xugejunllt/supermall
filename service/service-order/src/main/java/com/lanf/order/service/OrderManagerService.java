package com.lanf.order.service;

import com.lanf.api.goods.model.dto.ValidateCartDTO;
import com.lanf.order.model.bo.StartSubmitCartBO;
import com.lanf.order.model.dto.*;
import com.lanf.order.model.vo.CalculateOrderAmountVO;
import com.lanf.order.model.vo.PlaceOrderVO;
import com.lanf.order.model.vo.SubmitCartVO;
import com.lanf.order.model.vo.ValidateCartVO;
import com.lanf.seckill.mq.message.SecKillPlaneMessage;

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



    PlaceOrderVO startPlaceOrder(PlaceOrderDTO orderDTO);

    void  confirmPlaceOrder(PlaceOrderDTO orderDTO);

    void  cancelPlaceOrder(PlaceOrderDTO orderDTO);



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

    SubmitCartVO startSubmitCart(StartSubmitCartBO dto);

    void confirmSubmitCart(StartSubmitCartBO dto);

    void cancelSubmitCart(StartSubmitCartBO dto);
    /**
     * 取消订单
     *
     */
    void cancelOrder(CancelOrderDTO dto);

    /**
     * 创建秒杀单
     *
     */
    void createSecKillOrder(SecKillPlaneMessage message);

    /**
     * 发布商品评论
     *
     */
    void publishComment(PublishCommentDTO dto);

}
