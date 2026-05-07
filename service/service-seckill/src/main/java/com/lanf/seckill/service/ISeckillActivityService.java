package com.lanf.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.seckill.model.dto.AddSeckillActivityDTO;
import com.lanf.seckill.model.dto.AddSeckillItemDTO;
import com.lanf.seckill.model.entity.SeckillActivityDO;

/**
 * <p>
 * 秒杀活动表 服务类
 * </p>
 *
 * @author jarven
 * @since 2026-05-07
 */
public interface ISeckillActivityService extends IService<SeckillActivityDO> {

    /**
     * 添加秒杀活动
     * @param dto
     */
    void  addSeckillActivity(AddSeckillActivityDTO dto);

    /**
     * 添加秒杀商品
     *
     */
    void addAddSeckillItem(AddSeckillItemDTO dto);


}
