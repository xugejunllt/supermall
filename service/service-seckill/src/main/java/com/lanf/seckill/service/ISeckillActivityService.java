package com.lanf.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.seckill.model.dto.AddSeckillActivityDTO;
import com.lanf.seckill.model.dto.AddSeckillItemDTO;
import com.lanf.seckill.model.dto.LauncherSeckillItemDTO;
import com.lanf.seckill.model.entity.SeckillActivityDO;
import com.lanf.seckill.model.vo.SeckillItemDetailVO;
import com.lanf.seckill.model.vo.SeckillItemVO;
import java.util.List;

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

    /**
     * 上架秒杀商品
     *
     */
    void launcherSeckillItem(LauncherSeckillItemDTO itemDTO);

    /**
     * 分页查询秒杀商品列表
     * 
     * @param activityId 活动ID
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @return 秒杀商品VO列表
     */
    List<SeckillItemVO> pageQuerySeckillItems(Long activityId, int pageNum, int pageSize);

    /**
     * 查询秒杀商品详情
     * 
     * @param seckillItemId 秒杀商品ID
     * @return 商品详情VO
     */
    SeckillItemDetailVO getSeckillItemDetail( Long seckillItemId);





}
