package com.lanf.pay.service.pay;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.pay.model.bo.CancelPaidOrderBO;
import com.lanf.pay.model.bo.CancelWaitPayOrderBO;
import com.lanf.pay.model.entity.PaymentCancelRecordDO;
import com.lanf.rocketmq.exception.MessageRetryConsumeException;

/**
 * <p>
 * 三方支付订单取消记录 服务类
 * </p>
 *
 * @author jarven
 * @since 2026-04-19
 */
public interface IPaymentCancelRecordService extends IService<PaymentCancelRecordDO> {

    /**
     * 取消待支付订单
     *
     */
    void cancelWaitPayOrder(CancelWaitPayOrderBO cancelWaitPayOrderBO) throws MessageRetryConsumeException;



}
