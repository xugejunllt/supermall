package com.lanf.pay.mq.listener;

import com.lanf.api.pay.model.enums.PayChannelEnum;
import com.lanf.api.pay.model.enums.TransferEventTypeEnum;
import com.lanf.api.pay.mq.constant.PayClientTopicName;
import com.lanf.api.pay.mq.message.TransferMessage;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.constant.Constants;
import com.lanf.constant.utils.IdUtils;
import com.lanf.pay.model.entity.ClearingDetailDO;
import com.lanf.pay.model.entity.PayAccountDO;
import com.lanf.api.pay.model.enums.ClearingStatusEnum;
import com.lanf.pay.mq.constant.PayMqGroupName;
import com.lanf.pay.mq.constant.PayMqTopicName;
import com.lanf.pay.mq.message.ClearingOrderMessage;
import com.lanf.pay.service.account.IPayAccountService;
import com.lanf.pay.service.clearing.ClearingDetailService;
import com.lanf.rocketmq.exception.MessageRetryConsumeException;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 定时任务 扫描已关闭的订单
 * 主动检查 避免风险
 * 进行检查
 * 1.如果发生售后，检查退款是否成功
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = PayMqTopicName.SETTLEMENT_TASK_TOPIC,
        consumerGroup = PayMqGroupName.SETTLEMENT_TASK_GROUP
)
public class ClearingOrderListener implements RocketMQListener<ClearingOrderMessage> {

    @Autowired
    private ClearingDetailService clearingDetailService;
    //    @Autowired
//    private AfterSalesOrderApiService afterSalesOrderApiService;
    @Autowired
    private IPayAccountService payAccountService;
    @Autowired
    private RocketMqClient rocketMqClient;

    @Override
    public void onMessage(ClearingOrderMessage message) {
        log.info("收到结算任务消息: {}", message);

        Long liquidationId = message.getClearingDetailId();
        ClearingDetailDO liquidation = clearingDetailService.getById(liquidationId);
        if (liquidation == null) {
            log.error("清算单不存在: {}", liquidationId);
            return;
        }
        if (!ClearingStatusEnum.WAIT_CLEARING.equals(liquidation.getStatus())) {
            log.warn("清算单状态不是待结算: {}", liquidationId);
            return;
        }
//        UnderAfterSaleDTO underAfterSaleDTO = new UnderAfterSaleDTO();
//        underAfterSaleDTO.setOrderId(liquidation.getOrderId());

        /**
         * 发起转账
         */

        TransferMessage transferMessage = buildTransferMessage(liquidation);

        boolean update = clearingDetailService.lambdaUpdate()
                .eq(ClearingDetailDO::getId, liquidationId)
                .eq(ClearingDetailDO::getVersion, liquidation.getVersion())
                .set(ClearingDetailDO::getStatus, ClearingStatusEnum.CLEARING)
                .set(ClearingDetailDO::getVersion, liquidation.getVersion() + 1)
                .update();
        if (!update) {
            log.warn("更新清算单失败");
            throw new MessageRetryConsumeException("更新清算单失败");
        }
        rocketMqClient.sendMessage(PayClientTopicName.TRANSFER_TOPIC, JsonUtils.toJsonString(transferMessage));

    }

    private TransferMessage buildTransferMessage(ClearingDetailDO liquidation) {
        // 商家账户
        PayAccountDO account = payAccountService.getByTenantIdAccount(liquidation.getTenantId(), PayChannelEnum.ALI_PAY);
        //平台账户
        PayAccountDO platAccount = payAccountService.getByTenantIdAccount(Constants.PLATFORM_BUSINESS_ID, PayChannelEnum.ALI_PAY);
        Long liquidationId = liquidation.getId();
        TransferMessage transferMessage = new TransferMessage();
        transferMessage.setOutBizNo(IdUtils.generateId()+"");
        transferMessage.setMerchantId(liquidation.getTenantId());
        transferMessage.setBizOrderId(liquidationId);
        transferMessage.setEventType(TransferEventTypeEnum.ORDER_SETTLEMENT);
        //默认支付宝
        transferMessage.setTransferChannel(PayChannelEnum.ALI_PAY);
        transferMessage.setFromAccount(platAccount.getAccount());
        transferMessage.setIncomeAccount(account.getAccount());
        transferMessage.setTransAmount(liquidation.getIncomeMoney());
        transferMessage.setOrderTitle("订单结算");
        transferMessage.setIncomeAccountUserName(account.getAccountName());
        return transferMessage;
    }

}
