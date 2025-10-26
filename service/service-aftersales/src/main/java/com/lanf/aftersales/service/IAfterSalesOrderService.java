package com.lanf.aftersales.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.aftersales.model.dto.BusinessAgreeDTO;
import com.lanf.aftersales.model.dto.UserDeliveryDTO;
import com.lanf.aftersales.model.entity.AfterSalesOrderDO;
import com.lanf.aftersales.model.query.AfterSalesOrderPageQuery;
import com.lanf.aftersales.model.vo.AfterSalesOrderPageVO;
import com.lanf.mybatis.base.PageQuery;
import com.lanf.mybatis.base.PageResult;

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
     * 退货退款/换货 商家同意
     *
     */
    void  businessAgree(BusinessAgreeDTO dto);

    /**
     * 退货退款/换货 用户发货
     *
     */
    void userDelivery(UserDeliveryDTO dto);

    /**
     *
     * 换货出库单出库完成
     */
    void exchangeGoodsOutStockFinish(Long id);


    PageResult<AfterSalesOrderPageVO> afterSalesOrderPageQuery(AfterSalesOrderPageQuery query);

    AfterSalesOrderPageVO afterSalesOrderDetail(Long id);
}
