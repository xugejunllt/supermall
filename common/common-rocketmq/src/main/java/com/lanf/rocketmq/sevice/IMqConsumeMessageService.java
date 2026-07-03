package com.lanf.rocketmq.sevice;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.rocketmq.model.entity.MqConsumeMessageDO;
import com.lanf.rocketmq.model.query.MqConsumeMessagePageQuery;

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

    /**
     * 分页查询MQ消费消息
     *
     * @param query 分页查询条件
     * @return 分页结果
     */
    PageResult<MqConsumeMessageDO> mqConsumeMessagePageQuery(MqConsumeMessagePageQuery query);
}
