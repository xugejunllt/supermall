package com.lanf.aftersales.service.layout;

import com.lanf.aftersales.model.dto.AddAfterSalesOrderDTO;
import com.lanf.aftersales.model.dto.BusinessReceiverDTO;

/**
 * 接口编排
 */
public interface InterfaceLayoutService {


    /**
     * 创建售后单
     *
     */
    void afterSalesOrderAdd(AddAfterSalesOrderDTO dto);
    /**
     * 退货退款 商家收货
     *
     */
    void businessReceiver(BusinessReceiverDTO dto);


}
