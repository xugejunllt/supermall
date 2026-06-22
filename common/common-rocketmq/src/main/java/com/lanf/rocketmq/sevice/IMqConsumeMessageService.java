package com.lanf.rocketmq.sevice;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.rocketmq.model.entity.MqConsumeMessageDO;

/**
 * MQ本地事务消息消费 Service
 */
public interface IMqConsumeMessageService extends IService<MqConsumeMessageDO> {

    /**
     * 根据消息ID查询
     *
     * @param messageId 消息ID
     * @return 消息记录
     */
    MqConsumeMessageDO getByMessageId(String messageId);
}
