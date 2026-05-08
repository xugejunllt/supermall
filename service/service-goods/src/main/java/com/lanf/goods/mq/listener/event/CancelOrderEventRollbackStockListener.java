package com.lanf.goods.mq.listener.event;

import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.goods.model.entity.StockDO;
import com.lanf.goods.model.entity.UserStockFlowDO;
import com.lanf.goods.model.enums.StockFlowEventTypeEnum;
import com.lanf.goods.service.stock.IStockService;
import com.lanf.goods.service.stock.IUserStockFlowService;
import com.lanf.order.mq.constant.OrderClientTopicName;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.CancelOrderEventMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 回滚商品库存
 */

@Slf4j
@Component
@RocketMQMessageListener(topic = OrderClientTopicName.ORDER_CANCEL_EVENT_TOPIC,
        consumerGroup = TopicName.CANCEL_ORDER_EVENT_GOODS_GROUP)
public class CancelOrderEventRollbackStockListener implements RocketMQListener<CancelOrderEventMessage> {

    @Autowired
    private IStockService stockService;

    @Autowired
    private IUserStockFlowService userStockFlowService;

    @Transactional
    @Override
    public void onMessage(CancelOrderEventMessage message) {
        log.info("取消订单事件回滚库存开始:[{{}}]", JsonUtils.toJsonString(message));

        List<UserStockFlowDO> userStockFlowDOList = userStockFlowService.lambdaQuery()
                .eq(UserStockFlowDO::getOrderId, message.getOrderId())
                .eq(UserStockFlowDO::getEventType, StockFlowEventTypeEnum.CANCEL_ORDER_INBOUND.getCode())
                .in(UserStockFlowDO::getUserStockId, message.getSkuIdList())
                .list();
        if (userStockFlowDOList.isEmpty()){
            log.error("库存流水不存在");
            return;
        }
        List<Long> userStockIdList = userStockFlowDOList.stream().map(UserStockFlowDO::getUserStockId).collect(Collectors.toList());
        Map<Long, StockDO> idToStockMap = stockService.lambdaQuery()
                .in(StockDO::getId, userStockIdList)
                .list()
                .stream()
                .collect(Collectors.toMap(StockDO::getId, Function.identity()));


        Map<Long, Integer> stockQuantityMap = aggregateOutQuantityByUserStockId(userStockFlowDOList);
        List<UserStockFlowDO> saveUserStockFlowDOList =
                buildSaveUserStockFlowDOList(  stockQuantityMap, userStockFlowDOList, idToStockMap );

        try {

            userStockFlowService.saveBatch(saveUserStockFlowDOList);

        } catch (DuplicateKeyException e) {
            log.info("库存流水已存在");
            return;
        }
        for (Map.Entry<Long, Integer> entry : stockQuantityMap.entrySet()) {
            Long userStockId = entry.getKey();
            Integer rollbackQuantity = entry.getValue();

            StockDO stockDO = idToStockMap.get(userStockId);
            if (stockDO == null) {
                log.error("库存不存在，userStockId:{}", userStockId);
                return;
            }

            Long updateVersion = stockDO.getVersion() + 1;
            Integer updateUsableStock = stockDO.getUsableStock() + rollbackQuantity;

            boolean success = stockService.lambdaUpdate()
                    .eq(StockDO::getId, userStockId)
                    .eq(StockDO::getVersion, stockDO.getVersion())
                    .set(StockDO::getUsableStock, updateUsableStock)
                    .set(StockDO::getVersion, updateVersion)
                    .update();

            if (!success) {
                log.warn("库存回滚失败");
                throw new BizException("库存回滚失败");
            }
            log.info("库存回滚成功");
        }
    }




    /**
     * 聚合出库数量
     *
     *
     以userStockId 进行分组，并对同组的outQuantity进行累加，
     返回 Map -> key:userStockId, value:outQuantity
     */
    private Map<Long, Integer> aggregateOutQuantityByUserStockId(List<UserStockFlowDO> userStockFlowDOList) {
        return userStockFlowDOList.stream()
                .collect(Collectors.groupingBy(
                        UserStockFlowDO::getUserStockId,
                        Collectors.summingInt(UserStockFlowDO::getChangeQuantity)
                ));
    }

    private List<UserStockFlowDO> buildSaveUserStockFlowDOList( Map<Long, Integer> stockQuantityMap,
                                                                List<UserStockFlowDO> userStockFlowDOList,Map<Long, StockDO> idToStockMap ){

        List<UserStockFlowDO> saveUserStockFlowDOList =
                BeanCopyUtils.copyBeanList(userStockFlowDOList, UserStockFlowDO.class);

        saveUserStockFlowDOList.forEach(a -> {

            StockDO stockDO = idToStockMap.get(a.getUserStockId());
            if (stockDO == null) {
                log.error("库存不存在");
                return;
            }
            //变动的库存
            Integer changeQuantity = stockQuantityMap.get(a.getUserStockId());
            if (changeQuantity == null) {
                log.error("库存不存在");
                return;
            }

            Integer totalQuantity = stockDO.getUsableStock() + stockDO.getLockStock();
            a.setId(null);
            a.setEventType(StockFlowEventTypeEnum.CANCEL_ORDER_INBOUND.getCode());
            a.setAfterQuantity(totalQuantity);
            a.setBeforeQuantity(totalQuantity+changeQuantity);
            a.setChangeQuantity(changeQuantity);

        });

        return saveUserStockFlowDOList;
    }


}