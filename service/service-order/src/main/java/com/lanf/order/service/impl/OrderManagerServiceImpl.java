package com.lanf.order.service.impl;


import com.lanf.common.utils.IdUtils;
import com.lanf.constant.result.RpcResultParser;
import com.lanf.goods.api.GoodsApiService;
import com.lanf.goods.model.dto.DeductStockDTO;
import com.lanf.goods.model.vo.DeductStockVO;
import com.lanf.lock.aop.DistributedLock;
import com.lanf.order.model.bo.OrderInitParamsBO;
import com.lanf.order.model.dto.PlaceOrderDTO;
import com.lanf.order.model.vo.PlaceOrderVO;
import com.lanf.order.service.IOrderService;
import com.lanf.order.service.OrderManagerService;
import com.lanf.pay.api.PayApiService;
import com.lanf.welfare.api.WelfareApiService;
import com.lanf.welfare.model.dto.UseMultipleCouponDTO;
import com.lanf.welfare.model.vo.CalculateDiscountAmountVO;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hmily.annotation.HmilyTCC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
public class OrderManagerServiceImpl implements OrderManagerService {

    @Autowired
    private IOrderService orderService;

    @Autowired
    private PayApiService payApiService;

    @Autowired
    private GoodsApiService goodsApiService;

    @Autowired
    private WelfareApiService welfareApiService;

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
        /**
         * 扣减库存
         */
        DeductStockVO deductStockVO = deductStock(orderDTO, orderInitParamsBO);
        /**
         * 使用优惠卷
         */
        useMultipleCoupon( orderDTO,  orderInitParamsBO, deductStockVO.getTotalAmount());

        return null;
    }

    private void useMultipleCoupon(PlaceOrderDTO orderDTO, OrderInitParamsBO orderInitParamsBO, BigDecimal totalAmount){
        UseMultipleCouponDTO dto = new UseMultipleCouponDTO();
        dto.setOrderId(orderInitParamsBO.getOrderId());
        dto.setUserId(orderInitParamsBO.getUserId());
        dto.setShopId(orderDTO.getShopId());
        dto.setTotalAmount(totalAmount);
        dto.setCouponIds(orderDTO.getCouponIds());
        CalculateDiscountAmountVO amountVO = RpcResultParser.parseResult(welfareApiService.useMultipleCoupon(dto));


    }
    private DeductStockVO  deductStock( PlaceOrderDTO orderDTO, OrderInitParamsBO orderInitParamsBO){


        DeductStockDTO deductStockDTO = new DeductStockDTO();
        deductStockDTO.setOrderId(orderInitParamsBO.getOrderId());
        deductStockDTO.setSkuCode(orderDTO.getSkuCode());
        deductStockDTO.setQuantity(orderDTO.getQuantity());

        return RpcResultParser.parseResult(goodsApiService.deductStock(deductStockDTO));


    }

    private OrderInitParamsBO initParams(){

        OrderInitParamsBO  orderInitParamsBO = new OrderInitParamsBO();

        orderInitParamsBO.setOrderId(IdUtils.generateId());
        orderInitParamsBO.setUserId(IdUtils.generateId());
        return orderInitParamsBO;
    }
























}
