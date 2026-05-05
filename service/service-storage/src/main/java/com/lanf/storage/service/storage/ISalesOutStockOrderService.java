package com.lanf.storage.service.storage;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.aftersales.mq.message.SalesInStockOrderAddMessage;
import com.lanf.mybatis.base.PageResult;
import com.lanf.storage.model.dto.OutStockDTO;
import com.lanf.storage.model.entity.SalesOutStockOrderDO;
import com.lanf.storage.model.query.SalesOutStockOrderPageQuery;
import com.lanf.storage.model.vo.SalesOutStockOrderDetailVO;
import com.lanf.storage.model.vo.SalesOutStockOrderPageVO;

/**
 * <p>
 * 销售出库单 服务类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-09
 */
public interface ISalesOutStockOrderService extends IService<SalesOutStockOrderDO> {


    /**
     * 售后时 创建退货 商品入库单
     *
     */
    void salesStockOrderAdd(SalesInStockOrderAddMessage message);
    void  outStock(OutStockDTO dto);

    PageResult<SalesOutStockOrderPageVO>  salesOutStockOrderPage(SalesOutStockOrderPageQuery query);

    SalesOutStockOrderDetailVO salesOutStockOrderDetail(Long id);

}
