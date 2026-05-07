package com.lanf.seckill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.result.RpcResultParser;
import com.lanf.goods.api.GoodsApiService;
import com.lanf.goods.model.dto.SeckillStockPreoccupationDTO;
import com.lanf.seckill.mapper.SeckillActivityMapper;
import com.lanf.seckill.model.dto.AddSeckillActivityDTO;
import com.lanf.seckill.model.dto.AddSeckillItemDTO;
import com.lanf.seckill.model.entity.SeckillActivityDO;
import com.lanf.seckill.model.entity.SeckillItemDO;
import com.lanf.seckill.model.enums.SeckillActivityStatusEnum;
import com.lanf.seckill.service.ISeckillActivityService;
import com.lanf.seckill.service.ISeckillItemService;
import com.lanf.tcc.service.ITccOperationService;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hmily.annotation.HmilyTCC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Autowired
    private ITccOperationService tccOperationService ;
    @Autowired
    private ISeckillItemService seckillItemService;

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
    @HmilyTCC(confirmMethod = "confirmAddSeckillItem", cancelMethod = "cancelAddSeckillItem")
    @Override
    public void addAddSeckillItem(AddSeckillItemDTO dto) {

        Long activityId = dto.getActivityId();
        SeckillActivityDO one = this.lambdaQuery()
                .eq(SeckillActivityDO::getId, activityId)
                .one();
        if (one == null) {
            tccOperationService.addInterruptedFlag(buidSeckillItemKey(dto.getOrderNumber()),
                    "活动不存在");
            log.error("活动不存在");
            throw new BizException("活动不存在");
        }

        tccOperationService.tryOperation(buidSeckillItemKey(dto.getOrderNumber()), null);
        /**
         * 预占库存
         */
        seckillStockPreoccupation( dto);

    }


    private void seckillStockPreoccupation(AddSeckillItemDTO dto){
        SeckillStockPreoccupationDTO stockPreoccupationDTO = new SeckillStockPreoccupationDTO();
        stockPreoccupationDTO.setBizKeyPrx(dto.getOrderNumber());
        stockPreoccupationDTO.setSkuCode(dto.getSkuCode());
        stockPreoccupationDTO.setWarehouseId(dto.getWarehouseId());
        stockPreoccupationDTO.setPreQuantity(dto.getTotalStock());

        RpcResultParser.parseResult(goodsApiService.seckillStockPreoccupation(stockPreoccupationDTO));
    }

    private String buidSeckillItemKey(String bizKeyPrx) {
        return bizKeyPrx + "_" + "addAddSeckillItem";
    }
    @Transactional
    public void confirmAddSeckillItem(AddSeckillItemDTO dto) {

        SeckillItemDO seckillItemDO = new SeckillItemDO();
        seckillItemDO.setActivityId(dto.getActivityId());
        seckillItemDO.setItemId(dto.getItemId());
        seckillItemDO.setItemTitle(dto.getItemTitle());
        seckillItemDO.setItemImage(dto.getItemImage());
        seckillItemDO.setImages(dto.getImages());
        seckillItemDO.setSkuCode(dto.getSkuCode());
        seckillItemDO.setWarehouseId(dto.getWarehouseId());
        seckillItemDO.setAttributes(dto.getAttributes());
        seckillItemDO.setOriginalPrice(dto.getOriginalPrice());
        seckillItemDO.setSeckillPrice(dto.getSeckillPrice());
        seckillItemDO.setTotalStock(dto.getTotalStock());
        seckillItemDO.setLimitPerUser(dto.getLimitPerUser());
        seckillItemDO.setSoldStock(0);
        //默认下架状态
        seckillItemDO.setShelfStatus(0);
        seckillItemDO.setMerchantId(null);
        boolean operation = tccOperationService.confirmOperation(buidSeckillItemKey(dto.getOrderNumber()));
        if ( !operation){
            log.info("已执行");
            return;
        }
        seckillItemService.save(seckillItemDO);
    }
    public void cancelAddSeckillItem(AddSeckillItemDTO dto) {

        tccOperationService.cancelOperation(buidSeckillItemKey(dto.getOrderNumber()));

    }
}
