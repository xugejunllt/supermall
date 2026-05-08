package com.lanf.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.seckill.mapper.SecKillOrderMapper;
import com.lanf.seckill.model.entity.SecKillOrderDO;
import com.lanf.seckill.service.ISecKillOrderService;
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
public class SecKillOrderServiceImpl extends ServiceImpl<SecKillOrderMapper, SecKillOrderDO> implements ISecKillOrderService {

}
