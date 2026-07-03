package com.lanf.seckill.mq.listener;

import com.lanf.common.utils.CodeGenerateUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.utils.IdUtils;
import com.lanf.rocketmq.exception.MessageRetryConsumeException;
import com.lanf.rocketmq.util.RocketMqClient;
import com.lanf.seckill.api.SecKillResultCache;
import com.lanf.seckill.model.entity.SecKillItemDO;
import com.lanf.seckill.model.entity.SecKillRecordDO;
import com.lanf.seckill.model.enums.SecKillResultEnum;
import com.lanf.seckill.model.enums.SeckillModeEnum;
import com.lanf.seckill.mq.constant.SecKillClientTopicName;
import com.lanf.seckill.mq.constant.SecKillMqGroupName;
import com.lanf.seckill.mq.constant.SecKillMqTopicName;
import com.lanf.seckill.mq.message.SecKillMqExecuteMessage;
import com.lanf.seckill.mq.message.SecKillPlaneMessage;
import com.lanf.seckill.service.ISecKillItemService;
import com.lanf.seckill.service.ISecKillRecordService;
import com.lanf.seckill.service.strategy.SecKillStrategyFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 秒杀执行消息监听器
 * <p>
 * 1.监听秒杀请求消息，负责秒杀成功后的核心处理流程<br>
 * 2.处理流程：幂等去重 → 扣减秒杀库存 → 发送下游消息 → 标记秒杀结果<br>
 * 3.支持两种模式：REAL_TIME（实时秒杀，Redis已预扣库存）和 MQ_QUEUE（消息队列秒杀，在此扣减库存）<br>
 * 4.通过数据库唯一索引和乐观锁保证不超卖、不重复秒杀
 * </p>
 *
 * @author 
 * @since 
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = SecKillMqTopicName.SEC_KILL_MQ_EXECUTE_TOPIC,
        consumerGroup = SecKillMqGroupName.SEC_KILL_MQ_EXECUTE_GROUP
)
public class SecKillMqExecuteListener implements RocketMQListener<SecKillMqExecuteMessage> {

    @Autowired
    private ISecKillItemService secKillItemService;
    @Autowired
    private SecKillStrategyFactory secKillStrategyFactory;
    @Autowired
    private SecKillResultCache secKillResultCache;
    @Autowired
    private ISecKillRecordService secKillRecordService;
    @Autowired
    private RocketMqClient rocketMqClient;

    /**
     * 处理秒杀执行消息
     * <p>
     * 核心处理流程：<br>
     * 1.查询秒杀商品信息并生成订单ID<br>
     * 2.通过数据库唯一索引进行幂等去重，防止重复秒杀<br>
     * 3.MQ_QUEUE模式：使用乐观锁扣减秒杀商品库存<br>
     * 4.发送SEC_KILL_PLANE_TOPIC消息，通知订单服务和商品服务并行处理<br>
     * 5.标记秒杀结果为"订单生成中"，供前端轮询查询
     * </p>
     *
     * @param message 秒杀执行消息，包含用户ID、秒杀商品ID、秒杀模式等信息
     */
    @Transactional
    @Override
    public void onMessage(SecKillMqExecuteMessage message) {
        //1.记录日志，标识秒杀消息消费开始
        log.info("监听到秒杀成功消息,生成秒杀记录:{}",message);

        //2.查询秒杀商品详情，用于后续库存扣减和消息构建
        SecKillItemDO killItemDO = secKillItemService.getById(message.getSecKillItemId());

        //3.初始化秒杀参数：默认一次秒杀1个商品，预生成订单ID
        Integer stockQuantity = 1;
        Long orderId = IdUtils.generateId();

        //4.构建秒杀记录实体，用于幂等校验
        SecKillRecordDO recordDO = new SecKillRecordDO();
        recordDO.setUserId(message.getUserId());
        recordDO.setSecKillItemId(message.getSecKillItemId());
        recordDO.setStockQuantity(stockQuantity);
        recordDO.setTenantId(killItemDO.getTenantId());
        recordDO.setOrderId(orderId);

        //5.构建下游消息体，用于通知订单服务和商品服务处理
        SecKillPlaneMessage secKillPlaneMessage = buildSecKillPlaneMessage(killItemDO, message.getUserId(),
                stockQuantity,orderId);

        //6.幂等去重：通过数据库唯一索引(user_id + sec_kill_item_id)防止重复秒杀
        try {
            secKillRecordService.save(recordDO);
        } catch (DuplicateKeyException e) {
            //6.1 用户已秒杀成功，直接返回，不重复处理
            log.warn("用户已经秒杀成功");
            return;
        }

        //7.根据秒杀模式进行差异化处理
        if (SeckillModeEnum.MQ_QUEUE.equals(message.getSeckillModeEnum())){
            //7.1 MQ_QUEUE模式：在此真正扣减秒杀商品库存（Redis未预扣）
            log.info("消息队列秒杀模式");
            Integer remainingStock = killItemDO.getRemainingStock();
            //7.1.1 校验秒杀商品库存是否充足
            if (remainingStock <= 0) {
                log.error("库存不足");
                //7.1.2 库存不足，标记秒杀结果为"已售罄"并返回
                secKillResultCache.addResult(message.getUserId(), message.getSecKillItemId(),
                        SecKillResultEnum.SOLD_OUT);
                throw new BizException("库存不足");
            }
            //7.1.3 使用乐观锁扣减秒杀商品库存，保证并发安全
            boolean updated = secKillItemService.lambdaUpdate()
                    .eq(SecKillItemDO::getId, message.getSecKillItemId())
                    .eq(SecKillItemDO::getVersion, killItemDO.getVersion())
                    .set(SecKillItemDO::getRemainingStock, remainingStock - 1)
                    .set(SecKillItemDO::getVersion, killItemDO.getVersion() + 1)
                    .update();
            if (!updated) {
                //7.1.4 乐观锁更新失败，标记为已售罄，抛异常触发RocketMQ重试（默认3次）
                log.warn("更新秒杀商品失败,秒杀失败");

                throw new MessageRetryConsumeException("更新秒杀商品失败,秒杀失败");
            }

        } else {
            //7.2 REAL_TIME模式：Redis已预扣库存，此处无需扣减
            log.info("实时秒杀模式");
        }

        //8.发送SEC_KILL_PLANE_TOPIC消息，订单服务和商品服务并行消费
        //   订单服务：创建订单（TCC事务）
        //   商品服务：扣减真实库存
        rocketMqClient.sendMessage(SecKillClientTopicName.SEC_KILL_PLANE_TOPIC,
                JsonUtils.toJsonString(secKillPlaneMessage));

        //9.标记秒杀结果为"订单生成中"，供前端轮询查询
        secKillResultCache.addResult(message.getUserId(), message.getSecKillItemId(),
                SecKillResultEnum.SUCCESS_ORDER_CREATING);
    }

    /**
     * 构建秒杀平面消息
     * <p>
     * 将秒杀商品信息转换下游消息体，供订单服务和商品服务消费使用。<br>
     * 消息包含商品信息、用户信息、订单信息、价格信息、仓库信息等核心字段。
     * </p>
     *
     * @param killItemDO   秒杀商品实体，包含商品基本信息和秒杀配置
     * @param userId       用户ID，标识秒杀请求的用户
     * @param stockQuantity 秒杀商品数量，默认1个
     * @param orderId      预生成的订单ID，用于下游订单服务创建订单时关联
     * @return 秒杀平面消息，作为下游服务的消费载体
     */
    private SecKillPlaneMessage buildSecKillPlaneMessage(SecKillItemDO killItemDO ,Long userId,
                                                         Integer stockQuantity,Long orderId){
        //1.创建秒杀平面消息实体
        SecKillPlaneMessage secKillPlaneMessage = new SecKillPlaneMessage();

        //2.设置店铺信息
        secKillPlaneMessage.setShopId(killItemDO.getShopId());
        secKillPlaneMessage.setShopName(killItemDO.getShopName());

        //3.设置租户和用户信息
        Long tenantId = killItemDO.getTenantId();
        if (tenantId == null) {
            tenantId = 9999L;
        }
        secKillPlaneMessage.setTenantId(tenantId);
        secKillPlaneMessage.setUserId(userId);

        //4.设置订单基础信息
        secKillPlaneMessage.setOrderNumber(CodeGenerateUtils.generateOrderNumber());
        secKillPlaneMessage.setOrderId(orderId);

        //6.设置售后信息（秒杀商品默认无售后）

        //7.设置商品基本信息
        secKillPlaneMessage.setGoodsId(killItemDO.getItemId());
        secKillPlaneMessage.setGoodsName(killItemDO.getGoodsName());
        secKillPlaneMessage.setGoodsTitle(killItemDO.getItemTitle());

        //8.设置SKU信息
        secKillPlaneMessage.setSkuId(killItemDO.getSkuId());
        secKillPlaneMessage.setSkuName(killItemDO.getAttributes());
        secKillPlaneMessage.setSkuCode(killItemDO.getSkuCode());
        secKillPlaneMessage.setSkuPictureAddress(killItemDO.getSkuCode());

        //9.设置数量、价格和仓库信息
        secKillPlaneMessage.setQuantity(stockQuantity);
        secKillPlaneMessage.setUnitPrice(killItemDO.getSeckillPrice());
        secKillPlaneMessage.setWarehouseId(killItemDO.getWarehouseId());

        //10.设置版本信息（用于下游乐观锁校验）
        secKillPlaneMessage.setGoodsVersion(0L);
        secKillPlaneMessage.setSkuVersion(0L);

        //11.设置秒杀商品关联ID
        secKillPlaneMessage.setSecKillItemId(killItemDO.getId());

        return  secKillPlaneMessage;
    }
}
