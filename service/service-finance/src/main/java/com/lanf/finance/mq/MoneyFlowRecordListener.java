package com.lanf.finance.mq;

import com.lanf.common.utils.BigDecimalUtil;
import com.lanf.constant.exception.BizException;
import com.lanf.finance.model.entity.MoneyFlowDO;
import com.lanf.finance.model.entity.PayAccountDO;
import com.lanf.finance.model.enums.RecordTypeEnum;
import com.lanf.finance.mq.message.MoneyFlowMessage;
import com.lanf.finance.service.IMoneyFlowService;
import com.lanf.finance.service.IPayAccountService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
@RocketMQMessageListener(
    topic = FinanceClientTopicName.MONEY_FLOW_RECORD_TOPIC,
    consumerGroup = FinanceClientTopicName.MONEY_FLOW_RECORD_GROUP
)
public class MoneyFlowRecordListener implements RocketMQListener<MoneyFlowMessage> {

    @Autowired
    private IMoneyFlowService moneyFlowService;
    @Autowired
    private IPayAccountService payAccountService;

    private static final Set<Integer> INCOME_TYPE_SET = new HashSet<>(Arrays.asList(
            RecordTypeEnum.ORDER.getCode(),
            RecordTypeEnum.MERCHANT_SETTLEMENT_INCOME.getCode()
    ));

    private static final Set<Integer> EXPENSE_TYPE_SET = new HashSet<>(Arrays.asList(
            RecordTypeEnum.AFTER_SALES_REFUND.getCode(),
            RecordTypeEnum.CANCEL_ORDER_REFUND.getCode(),
            RecordTypeEnum.PLATFORM_SETTLEMENT_EXPENSE.getCode()
    ));

    @Override
    public void onMessage(MoneyFlowMessage message) {

        Long businessId = message.getBusinessId();
        PayAccountDO payAccountDO = payAccountService.lambdaQuery().eq(PayAccountDO::getBusinessId, businessId).one();

        if (payAccountDO == null){
            log.error("收支账户不存在");
            return;
        }
        String flowNo = generateFlowNo(message.getBizOrderId(),message.getRecordType().getCode());

        BigDecimal afterRemainMoney = calculateAfterRemainMoney(message.getRecordType(),
                message.getIncomeMoney(), payAccountDO.getRemainMoney());
        MoneyFlowDO moneyFlowDO = new MoneyFlowDO();
        // 生成流水号
        moneyFlowDO.setFlowNo(flowNo);
        // 设置其他字段
        moneyFlowDO.setBusinessId(message.getBusinessId());
        moneyFlowDO.setBizOrderId(message.getBizOrderId());
        moneyFlowDO.setRecordType(message.getRecordType());
        moneyFlowDO.setIncomeMoney(message.getIncomeMoney());
        moneyFlowDO.setIncomeAccount(payAccountDO.getAccount());
        moneyFlowDO.setBeforeRemainMoney(payAccountDO.getRemainMoney());
        moneyFlowDO.setAfterRemainMoney(afterRemainMoney);

        try {
            moneyFlowService.save(moneyFlowDO);
        } catch (DuplicateKeyException e) {
            log.warn("资金流水已存在");

        }
    }

    /**
     * 生成资金流水号
     * 格式: 业务订单ID + 记录类型code
     * 例如: 123456789_0 (订单ID为123456789, 类型为下单)
     * 对于售后单 部分退款,一笔售后单 一笔退款
     *
     */
    public static String generateFlowNo(Long bizOrderId, Integer recordTypeCode) {
        if (bizOrderId == null || recordTypeCode == null) {
            throw new IllegalArgumentException("业务订单ID和记录类型不能为空");
        }
        return bizOrderId + "_" + recordTypeCode;
    }
    private BigDecimal calculateAfterRemainMoney(RecordTypeEnum recordType, BigDecimal incomeMoney, BigDecimal beforeRemainMoney) {
        Integer code = recordType.getCode();

        if (INCOME_TYPE_SET.contains(code)) {
            return BigDecimalUtil.add(beforeRemainMoney, incomeMoney);
        } else if (EXPENSE_TYPE_SET.contains(code)) {
            return BigDecimalUtil.subtract(beforeRemainMoney, incomeMoney);
        } else {
            log.error("未知的记录类型:{}", code);
            throw new BizException("未知的记录类型:" + code);
        }
    }
}
