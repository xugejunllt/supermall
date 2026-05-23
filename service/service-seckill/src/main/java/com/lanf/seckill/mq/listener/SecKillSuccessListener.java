package com.lanf.seckill.mq.listener;

import com.lanf.common.utils.JsonUtils;
import com.lanf.rocketmq.util.RocketMqClient;
import com.lanf.seckill.model.entity.SecKillItemDO;
import com.lanf.seckill.model.entity.SecKillOrderDO;
import com.lanf.seckill.model.enums.SecKillOrderStatusEnum;
import com.lanf.seckill.mq.constant.SecKillMqGroupName;
import com.lanf.seckill.mq.constant.SecKillMqTopicName;
import com.lanf.seckill.mq.message.SecKillSuccessMessage;
import com.lanf.seckill.service.ISecKillItemService;
import com.lanf.seckill.service.ISecKillOrderService;
import com.lanf.seckill.service.strategy.SecKillResultCache;
import com.lanf.welfare.mq.constant.SecKillClientTopicName;
import com.lanf.welfare.mq.message.SecKillPlaneMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RocketMQMessageListener(
        topic = SecKillMqTopicName.SEC_KILL_SUCCESS_TOPIC,
        consumerGroup = SecKillMqGroupName.SEC_KILL_SUCCESS_GROUP
)
public class SecKillSuccessListener implements RocketMQListener<SecKillSuccessMessage> {

    @Autowired
    private ISecKillItemService secKillItemService;
    @Autowired
    private ISecKillOrderService secKillOrderService;
    @Autowired
    private RocketMqClient rocketMqClient;
    @Autowired
    private SecKillResultCache secKillResultCache;;
    @Transactional
    @Override
    public void onMessage(SecKillSuccessMessage message) {


        log.info("收到秒杀成功消息: userId={}, secKillId={}, orderNumber={}",
                message.getUserId(), 
                message.getSecKillItemId(),
                message.getOrderNumber());

        Long secKillItemId = message.getSecKillItemId();
        SecKillItemDO killItemDO = secKillItemService.getById(secKillItemId);

        SecKillOrderDO secKillOrderDO = getSecKillOrderDO(message, killItemDO);

        /**
         * 发送消息 创建订单
         *
         */
        SecKillPlaneMessage secKillPlaneMessage = new SecKillPlaneMessage();
        secKillPlaneMessage.setShopId(null);
        secKillPlaneMessage.setMerchantId(killItemDO.getTenantId());
        secKillPlaneMessage.setUserId(message.getUserId());
        secKillPlaneMessage.setOrderNumber(message.getOrderNumber());

        //取默认收货地址
        secKillPlaneMessage.setTakeAddress(null);
        //秒杀商品 默认无售后
        secKillPlaneMessage.setAfterSaleDays(0);
        secKillPlaneMessage.setGoodsId(killItemDO.getItemId());
        secKillPlaneMessage.setGoodsName(killItemDO.getGoodsName());
        secKillPlaneMessage.setGoodsTitle(killItemDO.getItemTitle());
        secKillPlaneMessage.setSkuId(killItemDO.getSkuId());
        secKillPlaneMessage.setSkuName(killItemDO.getAttributes());
        secKillPlaneMessage.setSkuPictureAddress(killItemDO.getSkuCode());
        secKillPlaneMessage.setQuantity(message.getItemQuantity());
        secKillPlaneMessage.setWarehouseId(killItemDO.getWarehouseId());
        secKillPlaneMessage.setGoodsVersion(killItemDO.getGoodsVersion());
        secKillPlaneMessage.setSkuVersion(killItemDO.getSkuVersion());

        try {
            secKillOrderService.save(secKillOrderDO);
        } catch (DuplicateKeyException e) {
            log.warn("该秒杀单已经创建");
        }
        rocketMqClient.sendMessage(SecKillClientTopicName.SEC_KILL_PLANE_TOPIC,
                JsonUtils.toJsonString(secKillPlaneMessage));

    }


    private static SecKillOrderDO getSecKillOrderDO(SecKillSuccessMessage message, SecKillItemDO killItemDO) {
        SecKillOrderDO secKillOrderDO = new SecKillOrderDO();
        secKillOrderDO.setUserId(message.getUserId());
        secKillOrderDO.setItemId(killItemDO.getItemId());
        secKillOrderDO.setActivityId(killItemDO.getActivityId());
        secKillOrderDO.setOrderNumber(message.getOrderNumber());
        secKillOrderDO.setItemQuantity(message.getItemQuantity());
        secKillOrderDO.setOrderStatus(SecKillOrderStatusEnum.CREATING);
        secKillOrderDO.setTenantId(killItemDO.getTenantId());
        return secKillOrderDO;
    }


}
