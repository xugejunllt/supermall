package com.lanf.order.service.impl;


import com.lanf.order.model.dto.PlaceOrderDTO;
import com.lanf.order.model.entity.OrderDO;
import com.lanf.order.model.vo.PlaceOrderVO;
import com.lanf.order.service.IOrderService;
import com.lanf.order.service.OrderManagerService;
import com.lanf.pay.api.PayApiService;
import com.lanf.pay.model.dto.PlaceSinglePayOrderDTO;
import com.lanf.security.utils.UserIdContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

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
    @Override
    public PlaceOrderVO placeOrder(PlaceOrderDTO orderDTO) {

        OrderDO orderDO = new OrderDO();
        orderDO.setShopId(1L);
        orderDO.setUserId(UserIdContext.getUserId());
        orderDO.setOrderNumber("121212");
        orderDO.setAddressId(1L);
        orderDO.setTakeAddress("takeAddress");
        orderDO.setDiscountAmount(new BigDecimal(1));
        orderDO.setTotalMoney(new BigDecimal(1));
        orderDO.setActualPayMoney(new BigDecimal(1));
        orderDO.setPlaceOrderTime(new Date());
        orderDO.setStatus(0);
        orderDO.setVersion(1L);
        orderService.save(orderDO);
        //
        PlaceSinglePayOrderDTO singlePayOrderDTO = new PlaceSinglePayOrderDTO();
        singlePayOrderDTO.setOrderNumber("121212");
        singlePayOrderDTO.setUserId(UserIdContext.getUserId());
        singlePayOrderDTO.setOrderId(orderDO.getId());
        singlePayOrderDTO.setPlaceOrderTime(new Date());
        singlePayOrderDTO.setTradeMoney(new BigDecimal(1));
        singlePayOrderDTO.setPayType(0);
        payApiService.placeSinglePayOrder(singlePayOrderDTO);
        return null;
    }
}
