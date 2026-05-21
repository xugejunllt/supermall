package com.lanf.pay.service.pay.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.api.pay.model.enums.PayChannelEnum;
import com.lanf.api.pay.model.enums.RefundEventTypeEnum;
import com.lanf.common.utils.JsonUtils;
import com.lanf.pay.mapper.RefundOrderMapper;
import com.lanf.pay.model.bo.CancelPaidOrderResultBO;
import com.lanf.pay.model.bo.ProcessRefund;
import com.lanf.pay.model.entity.PayOrderFlowDO;
import com.lanf.pay.model.entity.RefundOrderDO;
import com.lanf.pay.model.enums.RefundFlowStatusEnum;
import com.lanf.pay.model.enums.RefundStatusEnum;
import com.lanf.pay.mq.constant.PayMqTopicName;
import com.lanf.pay.mq.message.RefundQueryResultProcessorMessage;
import com.lanf.pay.service.pay.*;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * <p>
 * 退款单 服务实现类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-08-27
 */
@Slf4j
@Service
public class RefundOrderServiceImpl extends ServiceImpl<RefundOrderMapper, RefundOrderDO> implements IRefundOrderService {







}
