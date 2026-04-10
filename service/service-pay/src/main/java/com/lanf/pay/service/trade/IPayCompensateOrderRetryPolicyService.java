package com.lanf.pay.service.trade;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.pay.model.entity.PayCompensateOrderRetryPolicy;
import com.lanf.pay.model.vo.PayCompensateOrderRetryPolicyVO;

import java.util.List;

/**
 * <p>
 * 回调重试策略配置表 服务类
 * </p>
 *
 * @author jarven
 * @since 2026-04-07
 */
public interface IPayCompensateOrderRetryPolicyService extends IService<PayCompensateOrderRetryPolicy> {

    List<PayCompensateOrderRetryPolicyVO> getRetryPolicy();
}
