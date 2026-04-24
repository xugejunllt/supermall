package com.lanf.pay.service.pay;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.pay.model.bo.ProcessRefund;
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
     * 进行退款
     *
     */
    void processRefund(ProcessRefund processRefund);

}
