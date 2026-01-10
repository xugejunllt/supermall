package com.lanf.order.service.impl;


import com.lanf.common.utils.IdUtils;
import com.lanf.constant.result.RpcResultParser;
import com.lanf.goods.api.GoodsApiService;
import com.lanf.goods.model.dto.DeductStockDTO;
import com.lanf.lock.aop.DistributedLock;
import com.lanf.order.model.bo.OrderInitParamsBO;
import com.lanf.order.model.dto.PlaceOrderDTO;
import com.lanf.order.model.vo.PlaceOrderVO;
import com.lanf.order.service.IOrderService;
import com.lanf.order.service.OrderManagerService;
import com.lanf.pay.api.PayApiService;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hmily.annotation.HmilyTCC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Slf4j
@Service
public class OrderManagerServiceImpl implements OrderManagerService {

    @Autowired
    private IOrderService orderService;

    @Autowired
    private PayApiService payApiService;

    @Autowired
    private GoodsApiService goodsApiService;

    /**
     *立即下单
     *
     *
     */
    @HmilyTCC(confirmMethod = "confirmDoPlaceOrder", cancelMethod = "cancelDoPlaceOrder")
    @DistributedLock(key = "#orderDTO.orderNumber")
    @Override
    public PlaceOrderVO placeOrder(PlaceOrderDTO orderDTO) {

        OrderInitParamsBO orderInitParamsBO = initParams();
        //扣减库存
        deductStock( orderDTO,orderInitParamsBO);


        return null;
    }

    private void  deductStock( PlaceOrderDTO orderDTO, OrderInitParamsBO orderInitParamsBO){


        DeductStockDTO deductStockDTO = new DeductStockDTO();
        deductStockDTO.setOrderId(orderInitParamsBO.getOrderId());
        deductStockDTO.setSkuCode(orderDTO.getSkuCode());
        deductStockDTO.setQuantity(orderDTO.getQuantity());
        RpcResultParser.parseResult(goodsApiService.deductStock(deductStockDTO));


    }

    private OrderInitParamsBO initParams(){

        OrderInitParamsBO  orderInitParamsBO = new OrderInitParamsBO();

        orderInitParamsBO.setOrderId(IdUtils.generateId());

        return orderInitParamsBO;
    }
























}
