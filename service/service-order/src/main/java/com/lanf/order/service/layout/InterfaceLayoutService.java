package com.lanf.order.service.layout;

import com.lanf.goods.model.dto.CheckAndQueryGoodsDTO;
import com.lanf.order.model.dto.DeliveryDTO;
import com.lanf.order.model.dto.OnePlaceAnOrderDTO;
import com.lanf.order.model.dto.SubmitOrderDTO;
import com.lanf.order.model.vo.CreateOrderVO;
import com.lanf.order.model.vo.OnePlaceAnOrderVO;

/**
 * 接口编排
 */
public interface InterfaceLayoutService {
    /**
     * 提交订单 合并下单
     *
     */
    CreateOrderVO submitOrderDTO(SubmitOrderDTO dto);

    /**
     * 单笔下单
     *
     *
     */
    CreateOrderVO onePlaceAnOrder(OnePlaceAnOrderDTO dto);
    /**
     * 发货
     *
     */
    void  delivery(DeliveryDTO dto);



}
