package com.lanf.finance.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.finance.model.entity.ClearingOrderDO;
import com.lanf.finance.mapper.ClearingOrderMapper;
import com.lanf.finance.service.ClearingDetailService;
import com.lanf.finance.service.IClearingOrderService;
import com.lanf.finance.service.IPayAccountService;
import com.lanf.finance.service.ISettlementFlowService;
import com.lanf.client.pay.api.PayApiService;
import com.lanf.rocketmq.util.RocketMqClient;
import com.lanf.system.api.SystemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 清算单
 * 服务实现类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-20
 */
@Slf4j

@Service
public class ClearingOrderServiceImpl extends ServiceImpl<ClearingOrderMapper, ClearingOrderDO> implements IClearingOrderService {

    @Autowired
    private PayApiService payApiService;
    @Autowired
    private ClearingDetailService liquidationFlowService;
    @Autowired
    private RocketMqClient rocketMqClient;
    @Autowired
    private SystemService systemService;
    @Autowired
    private ISettlementFlowService settlementFlowService;
    @Autowired
    private IPayAccountService payAccountService;



}
