package com.lanf.finance.mq;

import com.lanf.common.utils.BigDecimalUtil;
import com.lanf.common.utils.IdUtils;
import com.lanf.finance.model.entity.LiquidationDO;
import com.lanf.finance.model.entity.LiquidationFlowDO;
import com.lanf.finance.model.enums.LiquidationStatusEnum;
import com.lanf.finance.model.enums.LiquidationTypeEnum;
import com.lanf.finance.service.ILiquidationFlowService;
import com.lanf.finance.service.ILiquidationService;
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

@Slf4j
@Component
@RocketMQMessageListener(
        topic = TopicName.TRADE_SUCCESS_EVENT_TOPIC,
        consumerGroup = TopicName.TRADE_SUCCESS_ORDER_GROUP)
public class TradeSuccessFinanceListener implements RocketMQListener<TradeSuccessEventMessage> {

    @Autowired
    private ILiquidationService liquidationService;
    private static final BigDecimal HUNDRED = new BigDecimal(100);

    private static final BigDecimal ZERO_POINT_ZERO_ONE = new BigDecimal("0.01");
    @Autowired
    private ILiquidationFlowService liquidationFlowService;
    /**
     * 平台费率 5%
     */
    private static final BigDecimal rate = new BigDecimal(5);

    @Transactional
    @Override
    public void onMessage(TradeSuccessEventMessage message) {

        log.info("监听清算事件:{}", message);

        List<LiquidationDO> liquidationDOList = new ArrayList<>();
        List<LiquidationFlowDO> liquidationFlowDOList = new ArrayList<>();
        List<OrderPayInfo> orderPayInfoList = message.getOrderPayInfoList();
        for (OrderPayInfo orderPayInfo : orderPayInfoList){
            Long orderId = orderPayInfo.getOrderId() ;

            BigDecimal payMoney = orderPayInfo.getPayMoney();
            Long  liquidationId = IdUtils.generateId();

            LiquidationDO liquidationDO = new LiquidationDO();
            liquidationDO.setOrderId(orderId);
            liquidationDO.setPayMoney(payMoney);
            liquidationDO.setId(liquidationId);
            liquidationDO.setPayType(orderPayInfo.getPayType());
            liquidationDO.setStatus(LiquidationStatusEnum.WAIT_SETTLEMENT);
            liquidationDOList.add(liquidationDO);
            //商家收入
            BigDecimal merchantIncomeMoney = calculateMerchantIncome(payMoney, rate);
            LiquidationFlowDO merchantLiquidationFlowDO = new LiquidationFlowDO();
            merchantLiquidationFlowDO.setLiquidationId(liquidationDO.getId());
            merchantLiquidationFlowDO.setMerchantId(orderPayInfo.getMerchantId());
            merchantLiquidationFlowDO.setLiquidationType(LiquidationTypeEnum.MERCHANT_INCOME);
            merchantLiquidationFlowDO.setRate(rate);
            merchantLiquidationFlowDO.setIncomeMoney(merchantIncomeMoney);
            liquidationFlowDOList.add(merchantLiquidationFlowDO);
        }

        liquidationService.saveBatch(liquidationDOList);
        liquidationFlowService.saveBatch(liquidationFlowDOList);

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