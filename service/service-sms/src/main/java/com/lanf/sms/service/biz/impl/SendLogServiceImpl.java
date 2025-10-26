package com.lanf.sms.service.biz.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.lanf.sms.mapper.SendLogMapper;
import com.lanf.sms.model.entity.SendLogDO;
import com.lanf.sms.service.biz.ISendLogService;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-07-30
 */
@Service
public class SendLogServiceImpl extends ServiceImpl<SendLogMapper, SendLogDO> implements ISendLogService {

}
