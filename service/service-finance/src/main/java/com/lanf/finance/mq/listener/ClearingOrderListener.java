package com.lanf.finance.mq.listener;

import com.lanf.aftersales.api.AfterSalesOrderApiService;
import com.lanf.aftersales.model.dto.UnderAfterSaleDTO;
import com.lanf.client.pay.model.enums.PayTypeEnum;
import com.lanf.client.pay.model.enums.TransferEventTypeEnum;
import com.lanf.client.pay.mq.constant.PayClientTopicName;
import com.lanf.client.pay.mq.message.TransferMessage;
import com.lanf.constant.constant.Constants;
import com.lanf.constant.result.Result;
import com.lanf.finance.model.entity.ClearingDetailDO;
import com.lanf.finance.model.entity.PayAccountDO;
import com.lanf.finance.model.enums.ClearingStatusEnum;
import com.lanf.finance.mq.constant.FinanceMqGroupName;
import com.lanf.finance.mq.constant.FinanceMqTopicName;
import com.lanf.finance.mq.message.ClearingOrderMessage;
import com.lanf.finance.service.ClearingDetailService;
import com.lanf.finance.service.IPayAccountService;
import com.lanf.rocketmq.exception.MessageRetryConsumeException;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 结算转账
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = FinanceMqTopicName.SETTLEMENT_TASK_TOPIC,
        consumerGroup = FinanceMqGroupName.SETTLEMENT_TASK_GROUP
)
public class ClearingOrderListener implements RocketMQListener<ClearingOrderMessage> {

    @Autowired
    private ClearingDetailService clearingDetailService;
    @Autowired
    private AfterSalesOrderApiService afterSalesOrderApiService;
    @Autowired
    private IPayAccountService payAccountService;
    @Autowired
    private RocketMqClient rocketMqClient;

    @Override
    public void onMessage(ClearingOrderMessage message) {
        log.info("收到结算任务消息: {}", message);

        Long liquidationId = message.getClearingDetailId();
        Long orderId = message.getOrderId();
        ClearingDetailDO liquidation = clearingDetailService.getById(liquidationId);
        if (liquidation == null) {
            log.error("清算单不存在: {}", liquidationId);
            return;
        }
        if (!ClearingStatusEnum.WAIT_CLEARING.equals(liquidation.getStatus())) {
            log.warn("清算单状态不是待结算: {}", liquidationId);
            return;
        }
        UnderAfterSaleDTO underAfterSaleDTO = new UnderAfterSaleDTO();
        underAfterSaleDTO.setOrderId(orderId);
        Result<Boolean> result = afterSalesOrderApiService.isUnderAfterSale(underAfterSaleDTO);

        if (result != null && Boolean.TRUE.equals(result.getData())) {
            log.info("订单 {} 正在售后中，暂不结算", orderId);
            return;
        }
        /**
         * 发起转账
         */

        TransferMessage transferMessage = buildTransferMessage(liquidation);

        boolean update = clearingDetailService.lambdaUpdate()
                .eq(ClearingDetailDO::getId, liquidationId)
                .eq(ClearingDetailDO::getStatus, ClearingStatusEnum.WAIT_CLEARING)
                .eq(ClearingDetailDO::getVersion, liquidation.getVersion())
                .set(ClearingDetailDO::getStatus, ClearingStatusEnum.CLEARING)
                .set(ClearingDetailDO::getVersion, liquidation.getVersion() + 1)
                .update();
        if (!update) {
            log.warn("更新清算单失败");
            throw new MessageRetryConsumeException("更新清算单失败");
        }
        rocketMqClient.sendMessage(PayClientTopicName.TRANSFER_TOPIC, transferMessage);

    }

    private TransferMessage buildTransferMessage(ClearingDetailDO liquidation){
        // 商家账户
        PayAccountDO merchantAccount = payAccountService.getByMerchantIdAccount(liquidation.getMerchantId(), PayTypeEnum.ALI_PAY.getCode());
        //平台账户
        PayAccountDO platAccount = payAccountService.getByMerchantIdAccount(Constants.PLATFORM_BUSINESS_ID, PayTypeEnum.ALI_PAY.getCode());
        Long liquidationId = liquidation.getId();
        String outBizNo = liquidationId.toString();
        TransferMessage transferMessage = new TransferMessage();
        transferMessage.setOutBizNo(outBizNo);
        transferMessage.setMerchantId(liquidation.getMerchantId());
        transferMessage.setBizOrderId(liquidationId);
        transferMessage.setEventType(TransferEventTypeEnum.ORDER_SETTLEMENT);
        //默认支付宝
        transferMessage.setTransferChannel(PayTypeEnum.ALI_PAY);
        transferMessage.setFromAccount(platAccount.getAccount());
        transferMessage.setIncomeAccount(merchantAccount.getAccount());
        transferMessage.setTransAmount(liquidation.getIncomeMoney());
        transferMessage.setOrderTitle("订单结算");
        return transferMessage;
    }
}
