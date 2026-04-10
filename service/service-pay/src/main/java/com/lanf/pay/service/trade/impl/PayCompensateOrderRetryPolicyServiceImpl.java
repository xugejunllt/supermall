package com.lanf.pay.service.trade.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.pay.mapper.PayCompensateOrderRetryPolicyMapper;
import com.lanf.pay.model.entity.PayCompensateOrderRetryPolicy;
import com.lanf.pay.model.vo.PayCompensateOrderRetryPolicyVO;
import com.lanf.pay.service.trade.IPayCompensateOrderRetryPolicyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class PayCompensateOrderRetryPolicyServiceImpl extends ServiceImpl<PayCompensateOrderRetryPolicyMapper, PayCompensateOrderRetryPolicy> implements IPayCompensateOrderRetryPolicyService {


    @Override
    public List<PayCompensateOrderRetryPolicyVO> getRetryPolicy() {

        List<PayCompensateOrderRetryPolicy> policies = this.lambdaQuery().eq(PayCompensateOrderRetryPolicy::getIsEnabled, 0).list();
        if (policies.isEmpty()){
            log.warn("未找到重试策略配置");
            return new ArrayList<>();
        }

        return BeanCopyUtils.copyBeanList(policies, PayCompensateOrderRetryPolicyVO.class);
    }
}
