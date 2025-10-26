package com.lanf.logistics.service.manager;

import com.lanf.logistics.model.bo.ExpressSubscribeBO;
import com.lanf.logistics.model.bo.ExpressQueryBO;
import com.lanf.logistics.model.bo.ExpressQueryResultBO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface LogisticsManagerService  {
    /**
     * 快递100查询
     */
    ExpressQueryResultBO expressQuery(ExpressQueryBO bo);

    /**
     * 快递订阅
     */
    void expressSubscribe(ExpressSubscribeBO expressSubscribeBO);

    /**
     * 快递推送
     *
     *
     */
    void expressPush (HttpServletRequest request, HttpServletResponse response,ExpressPushSuccessCallback callback);
}
