package com.lanf.finance.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.bizcache.service.BizCacheService;
import com.lanf.common.utils.BigDecimalUtil;
import com.lanf.common.utils.IdUtils;
import com.lanf.finance.model.entity.LiquidationDO;
import com.lanf.finance.model.entity.LiquidationFlowDO;
import com.lanf.finance.mapper.LiquidationMapper;
import com.lanf.finance.model.entity.PayAccountDO;
import com.lanf.finance.model.entity.SettlementFlowDO;
import com.lanf.finance.service.ILiquidationFlowService;
import com.lanf.finance.service.ILiquidationService;
import com.lanf.finance.service.IPayAccountService;
import com.lanf.finance.service.ISettlementFlowService;
import com.lanf.client.pay.api.PayApiService;
import com.lanf.client.pay.model.dto.TransferAccountsDTO;
import com.lanf.client.pay.model.vo.TransferAccountsVO;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.LiquidationDTO;
import com.lanf.rocketmq.model.message.MoneyFlowDTO;
import com.lanf.rocketmq.util.RocketMqClient;
import com.lanf.system.api.SystemService;
import com.lanf.system.model.vo.ShopVO;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
public class LiquidationServiceImpl extends ServiceImpl<LiquidationMapper, LiquidationDO> implements ILiquidationService {

    @Autowired
    private PayApiService payApiService;
    @Autowired
    private ILiquidationFlowService liquidationFlowService;
    @Autowired
    private RocketMqClient rocketMqClient;
    @Autowired
    private SystemService systemService;
    @Autowired
    private ISettlementFlowService settlementFlowService;
    @Autowired
    private IPayAccountService payAccountService;



}
