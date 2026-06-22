package com.lanf.rocketmq.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lanf.rocketmq.model.entity.MqConsumeMessageDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * MQ本地事务消息消费 Mapper
 */
@Mapper
public interface MqConsumeMessageMapper extends BaseMapper<MqConsumeMessageDO> {
}
