package com.lanf.rocketmq.sevice.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.rocketmq.mapper.MqSendMessageMapper;
import com.lanf.rocketmq.model.entity.MqSendMessageDO;
import com.lanf.rocketmq.model.query.MqSendMessagePageQuery;
import com.lanf.rocketmq.sevice.IMqSendMessageService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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

    @Override
    public PageResult<MqSendMessageDO> mqSendMessagePageQuery(MqSendMessagePageQuery query) {
        LambdaQueryWrapper<MqSendMessageDO> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(query.getTopic())) {
            wrapper.eq(MqSendMessageDO::getTopic, query.getTopic());
        }
        if (query.getStatus() != null) {
            wrapper.eq(MqSendMessageDO::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(MqSendMessageDO::getCreateTime);

        IPage<MqSendMessageDO> page = new Page<>(query.getPage(), query.getPageSize());
        page(page, wrapper);

        PageResult<MqSendMessageDO> result = new PageResult<>();
        result.setRecords(page.getRecords());
        result.setTotal(page.getTotal());
        result.setSize(page.getSize());
        return result;
    }
}
