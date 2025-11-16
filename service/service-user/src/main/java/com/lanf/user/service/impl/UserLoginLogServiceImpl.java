package com.lanf.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.user.mapper.UserLoginLogMapper;
import com.lanf.user.model.entity.UserLoginLog;
import com.lanf.user.service.IUserLoginLogService;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author jarven
 * @since 2025-11-15
 */
@Service
public class UserLoginLogServiceImpl extends ServiceImpl<UserLoginLogMapper, UserLoginLog> implements IUserLoginLogService {

}
