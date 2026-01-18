package com.lanf.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.order.model.dto.CreateOrderDTO2;
import com.lanf.order.model.entity.MainOrderDO;
import com.lanf.order.model.vo.CreateOrderVO;

/**
 * <p>
 * 主订单 服务类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-13
 */
public interface IMainOrderService extends IService<MainOrderDO> {



    /**
     * 创建订单
     * @param dto
     */
    CreateOrderVO createOrder(CreateOrderDTO2 dto);
}
