package com.lanf.pay.service.pay;

import com.baomidou.mybatisplus.extension.service.IService;
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
}
