package com.lanf.seckill.mq.constant;

public class SecKillMqTopicName {


    /**
     * 秒杀成功事件
     */
    public static final String SEC_KILL_SUCCESS_TOPIC = "SEC_KILL_SUCCESS_TOPIC";

    /**
     * MQ排队秒杀执行（真正扣减库存）
     */
    public static final String SEC_KILL_MQ_EXECUTE_TOPIC = "SEC_KILL_MQ_EXECUTE_TOPIC";

    /**
     * 秒杀优惠券执行
     */
    public static final String SEC_KILL_COUPON_MQ_EXECUTE_TOPIC = "SEC_KILL_COUPON_MQ_EXECUTE_TOPIC";
}
