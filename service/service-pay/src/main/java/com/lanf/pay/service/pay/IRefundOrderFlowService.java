package com.lanf.pay.service.pay;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.api.pay.model.query.RefundOrderFlowPageQuery;
import com.lanf.api.pay.model.vo.RefundOrderFlowPageVO;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.pay.model.entity.RefundOrderFlowDO;

import java.math.BigDecimal;

/**
 * <p>
 * 退款单表 服务类
 * </p>
 *
 * @author jarven
 * @since 2026-05-02
 */
public interface IRefundOrderFlowService extends IService<RefundOrderFlowDO> {

    /**
     * 根据日期统计退款金额
     */
    BigDecimal sumReturnMoney(String payFinishDate);

    /**
     * 分页查询退款单流水
     *
     *
     */
    PageResult<RefundOrderFlowPageVO> refundOrderFlowPageQuery(RefundOrderFlowPageQuery query);
}
