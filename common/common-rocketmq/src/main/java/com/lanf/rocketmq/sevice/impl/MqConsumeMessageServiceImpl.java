package com.lanf.rocketmq.sevice.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.rocketmq.mapper.MqConsumeMessageMapper;
import com.lanf.rocketmq.model.entity.MqConsumeMessageDO;
import com.lanf.rocketmq.model.query.MqConsumeMessagePageQuery;
import com.lanf.rocketmq.sevice.IMqConsumeMessageService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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

    @Override
    public PageResult<MqConsumeMessageDO> mqConsumeMessagePageQuery(MqConsumeMessagePageQuery query) {
        LambdaQueryWrapper<MqConsumeMessageDO> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(query.getMessageId())) {
            wrapper.eq(MqConsumeMessageDO::getMessageId, query.getMessageId());
        }
        if (StringUtils.hasText(query.getTopic())) {
            wrapper.eq(MqConsumeMessageDO::getTopic, query.getTopic());
        }
        if (StringUtils.hasText(query.getGroup())) {
            wrapper.eq(MqConsumeMessageDO::getGroup, query.getGroup());
        }
        if (query.getStatus() != null) {
            wrapper.eq(MqConsumeMessageDO::getStatus, query.getStatus());
        }


        wrapper.orderByDesc(MqConsumeMessageDO::getCreateTime);

        IPage<MqConsumeMessageDO> page = new Page<>(query.getPage(), query.getPageSize());
        page(page, wrapper);

        PageResult<MqConsumeMessageDO> result = new PageResult<>();
        result.setRecords(page.getRecords());
        result.setTotal(page.getTotal());
        result.setSize(page.getSize());
        return result;
    }
}
