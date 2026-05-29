package com.lanf.order.service.aftersales;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.api.order.model.dto.BusinessAgreeDTO;
import com.lanf.api.order.model.dto.BusinessReceiverDTO;
import com.lanf.api.order.model.dto.CompleteRefundDTO;
import com.lanf.constant.model.query.PageQuery;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.order.model.dto.*;
import com.lanf.order.model.entity.AfterSalesOrderDO;
import com.lanf.api.order.model.vo.AfterSalesOrderForUserDetailVO;
import com.lanf.api.order.model.vo.AfterSalesOrderForUserPageVO;

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

    PageResult<AfterSalesOrderForUserPageVO> afterSalesOrderForUserPageQuery(PageQuery query);

    AfterSalesOrderForUserDetailVO afterSalesOrderForUserDetailQuery(Long id);

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
     * 退货退款 商家收货
     *
     */
    void businessReceiver(BusinessReceiverDTO dto);


    /**
     * 完成退款
     *
     */
    void completeRefund(CompleteRefundDTO dto);

    /**
     * 销售出库单入库完成
     *
     */
    void afterSalesInStockFinish(Long id);
}
