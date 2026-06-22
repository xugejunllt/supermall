package com.lanf.rocketmq.sevice.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.rocketmq.mapper.MqConsumeMessageMapper;
import com.lanf.rocketmq.model.entity.MqConsumeMessageDO;
import com.lanf.rocketmq.sevice.IMqConsumeMessageService;
import org.springframework.stereotype.Service;

/**
 * MQ本地事务消息消费 ServiceImpl
 */
@Service
public class MqConsumeMessageServiceImpl extends ServiceImpl<MqConsumeMessageMapper, MqConsumeMessageDO> implements IMqConsumeMessageService {

    @Override
    public MqConsumeMessageDO getByMessageId(String messageId) {
        return getOne(new QueryWrapper<MqConsumeMessageDO>()
                .eq("message_id", messageId)
                .last("limit 1"));
    }
}
