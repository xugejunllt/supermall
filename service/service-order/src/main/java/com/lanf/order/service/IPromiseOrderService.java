package com.lanf.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.order.model.entity.PromiseOrderDO;

/**
 * <p>
 * 履约单 服务类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-08-29
 */
public interface IPromiseOrderService extends IService<PromiseOrderDO> {

    /**
     * 履约单退款
     *
     */
    void returnMoney(Long orderId);

    /**
     * 履约完成 进行退款
     *
     */
    void promiseOrderLiquidation(Long orderId);
}
