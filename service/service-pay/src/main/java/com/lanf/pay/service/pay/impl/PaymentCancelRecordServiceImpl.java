package com.lanf.pay.service.pay.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.pay.mapper.PaymentCancelRecordMapper;
import com.lanf.pay.model.entity.PaymentCancelRecordDO;
import com.lanf.pay.service.pay.IPaymentCancelRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 三方支付订单取消记录 服务实现类
 * </p>
 *
 * @author jarven
 * @since 2026-04-19
 */
@Slf4j
@Service
public class PaymentCancelRecordServiceImpl extends ServiceImpl<PaymentCancelRecordMapper, PaymentCancelRecordDO> implements IPaymentCancelRecordService {


}
