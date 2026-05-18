package com.lanf.order.utils;


import com.lanf.api.order.model.dto.CreateOrderDTO;
import com.lanf.constant.utils.IdUtils;
import com.lanf.order.model.entity.OrderDO;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class OrderServiceUtils {


    public static  String generateOrderNumber(){


        return IdUtils.generateId()+"";
    }

    public static OrderDO buildOrderDO(CreateOrderDTO dto){
//        List<DiscountInfoBO> discountInfoBO = dto.getDiscountInfoBO();
//        TakeAddressDTO takeAddressBO = dto.getTakeAddressBO();
//        String discountInfo = !IStringUtils.isEmpty(discountInfoBO) ? JsonUtils.toJsonString(discountInfoBO) : null;
//        String takeAddress = JsonUtils.toJsonString(takeAddressBO);
//        OrderDO orderDO = BeanCopyUtils.copyBean(dto, OrderDO.class);
//        orderDO.setId(dto.getOrderId());
//        orderDO.setStatus(OrderStatusEnum.WAIT_PAY);
//        orderDO.setDiscountInfo(discountInfo);
//        orderDO.setTakeAddress(takeAddress);
//        orderDO.setVersion(1L);
        /**
         * 售后期 写死7天
         */
//        orderDO.setAfterSaleDays(7);
        return null;
    }

}
