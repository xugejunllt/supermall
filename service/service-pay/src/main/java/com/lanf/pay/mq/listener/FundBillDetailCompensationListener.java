package com.lanf.pay.mq.listener;

import com.lanf.pay.mapper.FundBillDetailMapper;
import com.lanf.pay.model.entity.FundBillDetailDO;
import com.lanf.pay.mq.constant.PayMqGroupName;
import com.lanf.pay.mq.constant.PayMqTopicName;
import com.lanf.pay.mq.message.FundBillDetailCompensationMessage;
import com.lanf.pay.service.pay.IRefundOrderService;
import com.lanf.rocketmq.exception.MessageRetryConsumeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 解析账单 批量保存失败时
 * 通过mq进行补偿
 * mq可以自动进行重试
 *
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = PayMqTopicName.FUND_BILL_DETAIL_COMPENSATION_TOPIC,
        consumerGroup = PayMqGroupName.FUND_BILL_DETAIL_COMPENSATION_GROUP
)
public class FundBillDetailCompensationListener implements RocketMQListener<FundBillDetailCompensationMessage> {

    @Autowired
    private IRefundOrderService refundOrderService;
    @Autowired
    private FundBillDetailMapper fundBillDetailMapper;
    @Override
    public void onMessage(FundBillDetailCompensationMessage message) {

        List<FundBillDetailDO> cachedDataList = message.getCachedDataList();
        try {
            fundBillDetailMapper.batchInsertIgnore(cachedDataList);
        } catch (Exception e) {
           throw new MessageRetryConsumeException("批量插入对账单明细失败");
        }

    }

}
