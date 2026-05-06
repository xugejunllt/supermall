package com.lanf.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.order.model.entity.OrderItemDO;
import com.lanf.order.model.query.ReconciliationOrderItemQuery;
import com.lanf.order.model.vo.ReconciliationOrderItemVO;

/**
 * <p>
 * 订单商品项目 服务类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-13
 */
public interface IOrderItemService extends IService<OrderItemDO> {


    ReconciliationOrderItemVO reconciliationOrderItemQuery(ReconciliationOrderItemQuery  query);
}
