package com.lanf.rocketmq.sevice;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.rocketmq.model.entity.MqSendMessageDO;
import com.lanf.rocketmq.model.query.MqSendMessagePageQuery;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author jarven
 * @since 2026-06-20
 */
public interface IMqSendMessageService extends IService<MqSendMessageDO> {

    /**
     * 分页查询MQ发送消息
     *
     * @param query 分页查询条件
     * @return 分页结果
     */
    PageResult<MqSendMessageDO> mqSendMessagePageQuery(MqSendMessagePageQuery query);
}
