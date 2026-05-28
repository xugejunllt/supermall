package com.lanf.storage.service.storage;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.aftersales.mq.message.SalesInStockOrderAddMessage;
import com.lanf.api.storage.model.dto.AfterSalesIntStockDTO;
import com.lanf.api.storage.model.vo.AfterSalesIntStockOrderDetailVO;
import com.lanf.api.storage.model.vo.AfterSalesIntStockOrderPageVO;
import com.lanf.constant.model.query.PageQuery;
import com.lanf.constant.model.vo.PageResult;
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

    PageResult<AfterSalesIntStockOrderPageVO> afterSalesIntStockOrderPageQuery(PageQuery query);

    AfterSalesIntStockOrderDetailVO afterSalesIntStockOrderDetailQuery(Long id);

    /**
     *
     * 入库
     */
    void inStock(AfterSalesIntStockDTO dto );


}
