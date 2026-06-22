package com.lanf.rocketmq.sevice.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.rocketmq.mapper.MqSendMessageMapper;
import com.lanf.rocketmq.model.entity.MqSendMessageDO;
import com.lanf.rocketmq.sevice.IMqSendMessageService;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author jarven
 * @since 2026-06-20
 */
@Service
public class MqSendMessageServiceImpl extends ServiceImpl<MqSendMessageMapper, MqSendMessageDO> implements IMqSendMessageService {

}
