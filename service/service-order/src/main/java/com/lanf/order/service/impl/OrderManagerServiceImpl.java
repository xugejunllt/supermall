package com.lanf.order.service.impl;


import com.lanf.lock.aop.DistributedLock;
import com.lanf.order.model.dto.PlaceOrderDTO;
import com.lanf.order.model.entity.OrderDO;
import com.lanf.order.model.vo.PlaceOrderVO;
import com.lanf.order.service.IOrderService;
import com.lanf.order.service.OrderManagerService;
import com.lanf.pay.api.PayApiService;
import com.lanf.pay.model.dto.PlaceSinglePayOrderDTO;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hmily.annotation.HmilyTCC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
@Slf4j
@Service
public class OrderManagerServiceImpl implements OrderManagerService {

    @Autowired
    private IOrderService orderService;
    @Autowired
    private PayApiService payApiService;

    /**
     *立即下单
     *
     *
     */
    @HmilyTCC(confirmMethod = "confirmDoPlaceOrder", cancelMethod = "cancelDoPlaceOrder")
    @DistributedLock(key = "#orderDTO.orderNumber")
    @Override
    public PlaceOrderVO placeOrder(PlaceOrderDTO orderDTO) {

        log.info("立即下单开始");
        String orderNumber = orderDTO.getOrderNumber();
        OrderDO one = orderService.lambdaQuery().eq(OrderDO::getOrderNumber, orderNumber).one();
        if (one !=null){
            log.info("重复下单orderNumber[{}]",orderNumber);
            throw new RuntimeException("重复下单");
        }
        //
        PlaceSinglePayOrderDTO singlePayOrderDTO = new PlaceSinglePayOrderDTO();
        singlePayOrderDTO.setOrderNumber(orderDTO.getOrderNumber());
        singlePayOrderDTO.setUserId(11L);
        singlePayOrderDTO.setOrderId(orderDTO.getOrderId());
        singlePayOrderDTO.setPlaceOrderTime(new Date());
        singlePayOrderDTO.setTradeMoney(new BigDecimal(1));
        singlePayOrderDTO.setPayType(0);
        payApiService.placeSinglePayOrder(singlePayOrderDTO);
        return doPlaceOrder( orderDTO);
    }
    @Override
    public PlaceOrderVO doPlaceOrder(PlaceOrderDTO orderDTO) {


        return null;
    }

    public void confirmDoPlaceOrder(PlaceOrderDTO orderDTO) {
        log.info("立即下单开始confirm");

        OrderDO orderDO = new OrderDO();
        orderDO.setShopId(1L);
        orderDO.setUserId(11L);
        orderDO.setOrderNumber(orderDTO.getOrderNumber());
        orderDO.setAddressId(1L);
        orderDO.setTakeAddress("takeAddress");
        orderDO.setDiscountAmount(new BigDecimal(1));
        orderDO.setTotalMoney(new BigDecimal(1));
        orderDO.setActualPayMoney(new BigDecimal(1));
        orderDO.setPlaceOrderTime(new Date());
        orderDO.setStatus(0);
        orderDO.setVersion(1L);
        orderService.save(orderDO);

    }
    public void cancelDoPlaceOrder() {
        log.info("立即下单开始cancel");


    }
}
