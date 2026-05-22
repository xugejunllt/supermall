package com.lanf.order.service.aftersales;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.order.model.dto.AddAfterSalesOrderDTO;
import com.lanf.order.model.entity.AfterSalesOrderDO;

/**
 * <p>
 * 售后单 服务类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-19
 */
public interface IAfterSalesOrderService extends IService<AfterSalesOrderDO> {

    /**
     * 创建售后单
     *
     */
    void  addAfterSalesOrder(AddAfterSalesOrderDTO dto);


}
