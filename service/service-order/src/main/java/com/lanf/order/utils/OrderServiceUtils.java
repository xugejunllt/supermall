package com.lanf.order.utils;


import com.lanf.api.user.model.vo.AddressListVO;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.common.utils.IStringUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.model.enums.order.OrderStatusEnum;
import com.lanf.constant.utils.IdUtils;
import com.lanf.order.model.dto.CreateOrderDTO;
import com.lanf.order.model.entity.OrderDO;
import com.lanf.welfare.model.bo.DiscountInfoBO;
import lombok.extern.slf4j.Slf4j;

import java.util.List;


@Slf4j
public class OrderServiceUtils {


    public static  String generateOrderNumber(){


        return IdUtils.generateId()+"";
    }

    public static OrderDO buildOrderDO(CreateOrderDTO dto){
        List<DiscountInfoBO> discountInfoBO = dto.getDiscountInfoBOS();
        AddressListVO takeAddressBO = dto.getAddressListVO();
        String discountInfo = !IStringUtils.isEmpty(discountInfoBO) ? JsonUtils.toJsonString(discountInfoBO) : null;
        String takeAddress = JsonUtils.toJsonString(takeAddressBO);
        OrderDO orderDO = BeanCopyUtils.copyBean(dto, OrderDO.class);
        orderDO.setId(dto.getOrderId());
        orderDO.setStatus(OrderStatusEnum.WAIT_PAY);
        orderDO.setDiscountInfo(discountInfo);
        orderDO.setTakeAddress(takeAddress);
        orderDO.setVersion(1L);
        /**
         * 售后期 写死7天
         */
        orderDO.setAfterSaleDays(7);
        return orderDO;
    }

}
