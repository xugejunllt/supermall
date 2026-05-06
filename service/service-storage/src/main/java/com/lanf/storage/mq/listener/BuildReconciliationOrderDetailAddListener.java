package com.lanf.storage.mq.listener;

/**
 * 构建BuildReconciliationOrderDetail成功
 * 插入ReconciliationOrderDetail
 *
 */

import com.lanf.storage.model.entity.ReconciliationOrderDetailDO;
import com.lanf.storage.mq.constant.StorageClientTopicName;
import com.lanf.storage.mq.constant.StorageMqGroupName;
import com.lanf.storage.mq.message.BuildReconciliationOrderDetailMessage;
import com.lanf.storage.mq.message.ReconciliationOrderDetail;
import com.lanf.storage.service.reconciliation.IReconciliationOrderDetailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RocketMQMessageListener(topic = StorageClientTopicName.BUILD_RECONCILIATION_ORDER_DETAIL_TOPIC, consumerGroup =
        StorageMqGroupName.BUILD_RECONCILIATION_ORDER_DETAIL_GROUP)
public class BuildReconciliationOrderDetailAddListener implements RocketMQListener<BuildReconciliationOrderDetailMessage> {


    @Autowired
    private IReconciliationOrderDetailService reconciliationOrderDetailService;


    @Override
    public void onMessage(BuildReconciliationOrderDetailMessage message) {


        List<ReconciliationOrderDetailDO> reconciliationOrderDetailDOList = new ArrayList<>();
        String bathId = message.getBathId();

        for (ReconciliationOrderDetail detail : message.getOrderDetails()) {

            ReconciliationOrderDetailDO reconciliationOrderDetailDO = new ReconciliationOrderDetailDO();
            reconciliationOrderDetailDO.setBathId(bathId);
            reconciliationOrderDetailDO.setOrderId(detail.getOrderId());
            reconciliationOrderDetailDO.setOrderStatus(detail.getOrderStatus());
            reconciliationOrderDetailDOList.add(reconciliationOrderDetailDO);
        }
        try {
            reconciliationOrderDetailService.saveBatch(reconciliationOrderDetailDOList);
        } catch (DuplicateKeyException e) {
            log.warn("重复初始化");
            return;
        }


    }




}
