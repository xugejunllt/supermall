package com.lanf.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.api.order.model.query.OrderDocumentQuery;
import com.lanf.api.order.model.vo.OrderDocumentVO;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.order.model.dto.AllowOutboundDTO;
import com.lanf.order.model.dto.CreateOrderDTO;
import com.lanf.order.model.dto.DeliveryDTO;
import com.lanf.order.model.dto.SignForDTO;
import com.lanf.order.model.entity.OrderDO;
import com.lanf.order.model.query.AdminOrderSearchQuery;
import com.lanf.order.model.query.AppOrderSearchQuery;
import com.lanf.order.model.vo.AdminOrderListVO;
import com.lanf.order.model.vo.OrderListVO;

import java.util.List;

/**
 * <p>
 * 订单表 服务类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-13
 */
public interface IOrderService extends IService<OrderDO> {

    /**
     * 创建一笔订单
     *
     */

    void createOrder(CreateOrderDTO dto);

    /**
     * 允许出库
     *
     */
    void allowOutbound(AllowOutboundDTO dto);
    /**
     * 发货
     */
    void delivery(DeliveryDTO dto);

    /**
     * C端 搜索订单列表
     *
     */
    PageResult<OrderListVO> orderSearchQuery(AppOrderSearchQuery query);
    /**
     * admin 搜索订单列表
     *
     */
    PageResult<AdminOrderListVO>  orderSearchQuery(AdminOrderSearchQuery query);

    /**
     * 出库完成
     */
    void outStockFinish(Long orderId);

    /**
     * 签收
     */
    void signFor(SignForDTO dto);

    /**
     * 查询订单索引需要的字段
     *
     */
    OrderDocumentVO orderDocumentQuery(OrderDocumentQuery query);


    /**
     * 根据订单ID查询订单项的SKU ID列表
     *
     * @param orderId 订单ID
     * @return SKU ID列表
     */
    List<Long> querySkuIdsByOrderId(Long orderId);
    /**
     * 关闭超时未支付的订单
     *
     */
    void closeTimeOutNotPayOrder(Long orderId);



}
