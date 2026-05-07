package com.lanf.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.seckill.mapper.SeckillOrderMapper;
import com.lanf.seckill.model.entity.SeckillOrderDO;
import com.lanf.seckill.service.ISeckillOrderService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 秒杀订单表 服务实现类
 * </p>
 *
 * @author jarven
 * @since 2026-05-07
 */
@Service
public class SeckillOrderServiceImpl extends ServiceImpl<SeckillOrderMapper, SeckillOrderDO> implements ISeckillOrderService {

}
