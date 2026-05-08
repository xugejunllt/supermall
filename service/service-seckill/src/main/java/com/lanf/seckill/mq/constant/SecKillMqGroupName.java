package com.lanf.seckill.mq.constant;

public class SecKillMqGroupName {


    /**
     * 秒杀成功事件消费组
     */
    public static final String SEC_KILL_SUCCESS_GROUP = "SEC_KILL_SUCCESS_GROUP";
    public static final String SEC_KILL_STATUS_UPDATE_TOPIC = "SEC_KILL_STATUS_UPDATE_TOPIC";


    /**
     * MQ排队秒杀执行消费组（真正扣减库存）
     */
    public static final String SEC_KILL_MQ_EXECUTE_GROUP = "SEC_KILL_MQ_EXECUTE_GROUP";
}
