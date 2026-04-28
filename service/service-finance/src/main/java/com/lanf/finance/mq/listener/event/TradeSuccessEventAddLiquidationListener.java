package com.lanf.finance.mq.listener.event;

import com.lanf.common.utils.BigDecimalUtil;
import com.lanf.finance.model.entity.ClearingDetailDO;
import com.lanf.finance.model.enums.ClearingStatusEnum;
import com.lanf.finance.model.enums.RecipientTypeEnum;
import com.lanf.finance.service.ClearingDetailService;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.OrderPayInfo;
import com.lanf.rocketmq.model.message.TradeSuccessEventMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 添加清分单
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = TopicName.TRADE_SUCCESS_EVENT_TOPIC,
        consumerGroup = TopicName.TRADE_SUCCESS_ORDER_GROUP)
public class TradeSuccessEventAddLiquidationListener implements RocketMQListener<TradeSuccessEventMessage> {

    @Autowired
    private ClearingDetailService clearingDetailService;

    private static final BigDecimal HUNDRED = new BigDecimal(100);

    private static final BigDecimal ZERO_POINT_ZERO_ONE = new BigDecimal("0.01");

    /**
     * 平台费率 5%
     */
    private static final BigDecimal rate = new BigDecimal(5);

    @Transactional
    @Override
    public void onMessage(TradeSuccessEventMessage message) {

        log.info("监听清算事件:{}", message);

        List<ClearingDetailDO> clearingDetailDOList = new ArrayList<>();
        List<OrderPayInfo> orderPayInfoList = message.getOrderPayInfoList();
        for (OrderPayInfo orderPayInfo : orderPayInfoList){
            Long orderId = orderPayInfo.getOrderId() ;
            BigDecimal payMoney = orderPayInfo.getPayMoney();
            //商家收入
            BigDecimal merchantIncomeMoney = calculateMerchantIncome(payMoney, rate);
            ClearingDetailDO merchantLiquidationFlowDO = new ClearingDetailDO();
            merchantLiquidationFlowDO.setOrderId(orderId);
            merchantLiquidationFlowDO.setPayMoney(payMoney);
            merchantLiquidationFlowDO.setMerchantId(orderPayInfo.getMerchantId());
            merchantLiquidationFlowDO.setStatus(ClearingStatusEnum.WAIT_CLEARING);
            merchantLiquidationFlowDO.setRecipientType(RecipientTypeEnum.MERCHANT);
            merchantLiquidationFlowDO.setIncomeMoney(merchantIncomeMoney);
            merchantLiquidationFlowDO.setVersion(1L);
            clearingDetailDOList.add(merchantLiquidationFlowDO);
        }

        clearingDetailService.saveBatch(clearingDetailDOList);

    }


    /**
     * 计算商家收入金额
     * 公式: 商家收入 = 支付金额 × (100 - 费率) × 0.01
     * 例如: 支付100元, 费率5%, 商家收入 = 100 × (100-5) × 0.01 = 95元
     *
     * @param payMoney 支付金额
     * @param rate 费率百分比
     * @return 商家收入金额
     */
    private BigDecimal calculateMerchantIncome(BigDecimal payMoney, BigDecimal rate) {
        BigDecimal rateFactor = BigDecimalUtil.subtract(HUNDRED, rate);
        return BigDecimalUtil.multiply(payMoney, rateFactor).multiply(ZERO_POINT_ZERO_ONE);
    }
}