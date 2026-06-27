package com.lanf.order.service.order.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.order.mapper.OrderAutoCloseMapper;
import com.lanf.order.model.entity.OrderAutoCloseDO;
import com.lanf.order.service.order.IOrderAutoCloseService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 订单签收后的自动关闭超时时间 服务实现类
 * </p>
 *
 * @author jarven
 * @since 2026-06-27
 */
@Service
public class OrderAutoCloseServiceImpl extends ServiceImpl<OrderAutoCloseMapper, OrderAutoCloseDO> implements IOrderAutoCloseService {

}
