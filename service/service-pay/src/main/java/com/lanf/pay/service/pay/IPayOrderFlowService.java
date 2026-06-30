package com.lanf.pay.service.pay;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.pay.model.entity.PayOrderFlowDO;

import java.math.BigDecimal;

/**
 * <p>
 * 支付流水
 * </p>
 *
 * @author jarven
 * @since 2025-12-28
 */
public interface IPayOrderFlowService extends IService<PayOrderFlowDO> {

    /**
     * 根据日期统计实收金额
     */
    BigDecimal sumReceiptMoney(String payFinishDate);
}
