package com.lanf.order.service.shipping;

import com.lanf.order.model.bo.ShippingSubscribeBO;
import com.lanf.order.model.bo.ShippingVO;
import com.lanf.order.model.query.ShippingQuery;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface Kuaidi100Service {


    /**
     * 快递100查询
     */
    ShippingVO shippingQuery(ShippingQuery query);

    /**
     * 快递订阅
     */
    void subscribe(ShippingSubscribeBO expressSubscribeBO);

    /**
     * 快递100物流轨迹推送
     *
     *
     */
    void shippingCallback(HttpServletRequest request, HttpServletResponse response);
}
