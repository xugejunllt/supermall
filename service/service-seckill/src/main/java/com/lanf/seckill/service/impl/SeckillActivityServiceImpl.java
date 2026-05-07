package com.lanf.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.constant.exception.BizException;
import com.lanf.goods.api.GoodsApiService;
import com.lanf.seckill.mapper.SeckillActivityMapper;
import com.lanf.seckill.model.dto.AddSeckillActivityDTO;
import com.lanf.seckill.model.dto.AddSeckillItemDTO;
import com.lanf.seckill.model.entity.SeckillActivityDO;
import com.lanf.seckill.model.enums.SeckillActivityStatusEnum;
import com.lanf.seckill.service.ISeckillActivityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * <p>
 * 秒杀活动表 服务实现类
 * </p>
 *
 * @author jarven
 * @since 2026-05-07
 */
@Slf4j
@Service
public class SeckillActivityServiceImpl extends ServiceImpl<SeckillActivityMapper, SeckillActivityDO> implements ISeckillActivityService {

    @Autowired
    private GoodsApiService goodsApiService;


    @Override
    public void addSeckillActivity(AddSeckillActivityDTO dto) {

        Date endTime = dto.getEndTime();

        if (endTime.before(new Date())) {
            log.warn("活动结束时间不能早于当前时间");
            throw new BizException("活动结束时间不能早于当前时间");
        }

        SeckillActivityDO seckillActivityDO = new SeckillActivityDO();
        seckillActivityDO.setName(dto.getName());
        seckillActivityDO.setStartTime(dto.getStartTime());
        seckillActivityDO.setEndTime(dto.getEndTime());
        seckillActivityDO.setStatus(SeckillActivityStatusEnum.NOT_STARTED);
        seckillActivityDO.setMerchantId(null);
        this.save(seckillActivityDO);

    }

    @Override
    public void addAddSeckillItem(AddSeckillItemDTO dto) {


        goodsApiService.seckillStockPreoccupation(dto)

    }
}
