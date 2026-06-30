package com.lanf.pay.service.pay;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.api.pay.model.query.RefundOrderPageQuery;
import com.lanf.api.pay.model.vo.RefundOrderPageVO;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.pay.model.entity.RefundOrderDO;

/**
 * <p>
 * 退款单 服务类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-08-27
 */
public interface IRefundOrderService extends IService<RefundOrderDO> {

    /**
     * 分页查询退款单
     *
     *
     */
    PageResult<RefundOrderPageVO> refundOrderPageQuery(RefundOrderPageQuery query);

}
