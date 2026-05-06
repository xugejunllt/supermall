package com.lanf.finance.mq.listener.event;

import com.lanf.common.utils.BigDecimalUtil;
import com.lanf.common.utils.JsonUtils;
import com.lanf.finance.model.entity.ClearingDetailDO;
import com.lanf.finance.model.enums.ClearingStatusEnum;
import com.lanf.finance.model.enums.RecipientTypeEnum;
import com.lanf.finance.mq.constant.FinanceMqGroupName;
import com.lanf.finance.service.ClearingDetailService;
import com.lanf.order.mq.constant.OrderClientTopicName;
import com.lanf.order.mq.message.SignOrderMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;


/**
 * 订单签收时 进行新增计算单
 */

@Slf4j
@Component
@RocketMQMessageListener(topic = OrderClientTopicName.SIGN_ORDER_EVENT_TOPIC, consumerGroup = FinanceMqGroupName.SIGN_ORDER_EVENT_FINANCE_GROUP)
public class SignOrderEventAddClearingDetailListener implements RocketMQListener<SignOrderMessage> {

    @Autowired
    private ClearingDetailService clearingDetailService;
    private static final BigDecimal HUNDRED = new BigDecimal(100);

    private static final BigDecimal ZERO_POINT_ZERO_ONE = new BigDecimal("0.01");

    /**
     * 平台费率 5%
     */
    private static final BigDecimal rate = new BigDecimal(5);

    @Override
    public void onMessage(SignOrderMessage message) {

        log.info("订单签收时开始:[{{}}]", JsonUtils.toJsonString(message));
        Long orderId = message.getOrderId();
        Date signTime = message.getSignTime();
        BigDecimal payMoney = message.getPayMoney();
        Integer afterSaleDays = message.getAfterSaleDays();
        Long merchantId = message.getMerchantId();
        Date afterSaleExpireTime = new Date(signTime.getTime() + afterSaleDays * 24 * 60 * 60 * 1000);

        //商家收入
        BigDecimal merchantIncomeMoney = calculateMerchantIncome(payMoney, rate);
        ClearingDetailDO merchantLiquidationFlowDO = new ClearingDetailDO();
        merchantLiquidationFlowDO.setOrderId(orderId);
        merchantLiquidationFlowDO.setPayMoney(payMoney);
        merchantLiquidationFlowDO.setMerchantId(merchantId);
        merchantLiquidationFlowDO.setStatus(ClearingStatusEnum.WAIT_CLEARING);
        merchantLiquidationFlowDO.setRecipientType(RecipientTypeEnum.MERCHANT);
        merchantLiquidationFlowDO.setIncomeMoney(merchantIncomeMoney);
        merchantLiquidationFlowDO.setAfterSaleExpireTime(afterSaleExpireTime);
        merchantLiquidationFlowDO.setVersion(1L);
        try {
            clearingDetailService.save(merchantLiquidationFlowDO);
        } catch (DuplicateKeyException e) {
            log.warn("订单已创建结算单");
        }

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