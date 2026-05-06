package com.lanf.storage.mq.listener;

/**
 * 售后退货创建商品入库单
 */

import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.result.RpcResultParser;
import com.lanf.goods.api.GoodsApiService;
import com.lanf.goods.model.query.ReconciliationStockFlowQuery;
import com.lanf.goods.model.vo.ReconciliationStockFlowVO;
import com.lanf.order.api.OrderApiService;
import com.lanf.order.model.enums.OrderStatusEnum;
import com.lanf.order.model.query.ReconciliationOrderItemQuery;
import com.lanf.order.model.vo.ReconciliationOrderItem;
import com.lanf.order.model.vo.ReconciliationOrderItemVO;
import com.lanf.rocketmq.exception.MessageRetryConsumeException;
import com.lanf.storage.model.entity.ReconciliationOrderDetailDO;
import com.lanf.storage.model.enums.ReconciliationOrderStatusEnum;
import com.lanf.storage.mq.constant.StorageClientTopicName;
import com.lanf.storage.mq.constant.StorageMqGroupName;
import com.lanf.storage.mq.message.AddReconciliationOrderDetail;
import com.lanf.storage.service.reconciliation.IReconciliationOrderDetailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RocketMQMessageListener(topic = StorageClientTopicName.ADD_RECONCILIATION_ORDER_TOPIC, consumerGroup =
        StorageMqGroupName.ADD_RECONCILIATION_ORDER_GROUP)
public class AddReconciliationOrderDetailListener implements RocketMQListener<AddReconciliationOrderDetail> {

    @Autowired
    private OrderApiService orderApiService;
    @Autowired
    private IReconciliationOrderDetailService reconciliationOrderDetailService;
    @Autowired
    private GoodsApiService goodsApiService;


    @Override
    public void onMessage(AddReconciliationOrderDetail message) {


        Long orderId = message.getOrderId();
        ReconciliationOrderItemQuery query = new ReconciliationOrderItemQuery();
        query.setOrderId(orderId);
        query.setOrderStatus(message.getToOrderStatus());

        OrderStatusEnum orderStatus = message.getToOrderStatus();
        ReconciliationOrderStatusEnum reconciliationOrderStatus = null;
        switch (orderStatus) {
            case WAIT_PAY:
                reconciliationOrderStatus = ReconciliationOrderStatusEnum.PENDING_OUTBOUND;
                break;
            case OUTBOUNDED:
                reconciliationOrderStatus = ReconciliationOrderStatusEnum.OUTBOUNDED;
                break;
            case CANCELLED:
                reconciliationOrderStatus = ReconciliationOrderStatusEnum.CANCELLED;
                break;

        }
        ReconciliationOrderDetailDO oned = reconciliationOrderDetailService.lambdaQuery()
                .eq(ReconciliationOrderDetailDO::getOrderId, orderId)
                .eq(ReconciliationOrderDetailDO::getOrderStatus, reconciliationOrderStatus)
                .one();
        if (oned != null) {
            log.warn("订单入库单已存在");
            return;
        }
        ReconciliationOrderItemVO parseResult = null;
        try {
             parseResult = RpcResultParser.parseResult(orderApiService.reconciliationOrderItemQuery(query));
        } catch (Exception e) {
           throw new MessageRetryConsumeException("查询订单轨迹异常");
        }

        ReconciliationStockFlowQuery query2 = new ReconciliationStockFlowQuery();
        query2.setOrderId(orderId);
        query2.setUserStockFlowEventType(message.getUserStockFlowEventType());
        ReconciliationStockFlowVO flowVO = null;
        try {
             flowVO = RpcResultParser.parseResult(goodsApiService.reconciliationStockFlowQuery(query2));
        } catch (Exception e) {
            log.warn("查询商品入库单异常");
            throw new MessageRetryConsumeException(" 查询商品入库单异常");
        }


        List<ReconciliationOrderItem> orderItemVOS = parseResult.getOrderItemVOS();
        ReconciliationOrderDetailDO reconciliationOrderDetailDO = new ReconciliationOrderDetailDO();
        reconciliationOrderDetailDO.setOrderId(orderId);
        reconciliationOrderDetailDO.setOrderStatus(reconciliationOrderStatus);
        reconciliationOrderDetailDO.setOrderItems(JsonUtils.toJsonString(orderItemVOS));
        reconciliationOrderDetailDO.setBathId(parseResult.getCreateDate());
        reconciliationOrderDetailDO.setStockFlows(JsonUtils.toJsonString(flowVO.getReconciliationStockFlowList()));
        try {
            reconciliationOrderDetailService.save(reconciliationOrderDetailDO);
        } catch (DuplicateKeyException e) {
             log.warn("订单入库单已存在");
        }

    }




}
