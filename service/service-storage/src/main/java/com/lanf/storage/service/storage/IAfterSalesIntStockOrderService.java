package com.lanf.storage.service.storage;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.aftersales.mq.message.SalesInStockOrderAddMessage;
import com.lanf.storage.model.dto.AfterSalesIntStockDTO;
import com.lanf.storage.model.entity.AfterSalesIntStockOrderDO;

/**
 * <p>
 * 售后出库单 服务类
 * </p>
 *
 * @author jarven
 * @since 2026-04-24
 */
public interface IAfterSalesIntStockOrderService extends IService<AfterSalesIntStockOrderDO> {

    /**
     * 添加售后入库单
     *
     */
    void addAfterSalesIntStockOrder(SalesInStockOrderAddMessage message);

    /**
     *
     * 入库
     */
    void inStock(AfterSalesIntStockDTO dto );


}
