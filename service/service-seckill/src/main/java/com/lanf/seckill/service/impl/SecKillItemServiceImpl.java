package com.lanf.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.seckill.mapper.SecKillItemMapper;
import com.lanf.seckill.model.entity.SecKillItemDO;
import com.lanf.seckill.service.ISecKillItemService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 秒杀商品表 服务实现类
 * </p>
 *
 * @author jarven
 * @since 2026-05-07
 */
@Service
public class SecKillItemServiceImpl extends ServiceImpl<SecKillItemMapper, SecKillItemDO> implements ISecKillItemService {

}
