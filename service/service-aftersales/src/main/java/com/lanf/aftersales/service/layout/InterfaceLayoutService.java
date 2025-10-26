package com.lanf.aftersales.service.layout;

import com.lanf.aftersales.model.dto.AfterSalesOrderAddDTO;
import com.lanf.aftersales.model.dto.BusinessReceiverDTO;
import com.lanf.aftersales.model.dto.ExchangeGoodsBusinessDeliveryDTO;
import com.lanf.aftersales.model.dto.ExchangeGoodsCreateOutStockOrderDTO;

/**
 * 接口编排
 */
public interface InterfaceLayoutService {


    /**
     * 创建售后单
     *
     */
    void afterSalesOrderAdd(AfterSalesOrderAddDTO dto);
    /**
     * 退货退款 商家收货
     *
     */
    void businessReceiver(BusinessReceiverDTO dto);


}
