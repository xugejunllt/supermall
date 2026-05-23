package com.lanf.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.seckill.model.dto.AddSeckillActivityDTO;
import com.lanf.seckill.model.dto.AddSeckillItemDTO;
import com.lanf.seckill.model.dto.GetSeckillTokenDTO;
import com.lanf.seckill.model.dto.LauncherSeckillItemDTO;
import com.lanf.seckill.model.entity.SecKillActivityDO;
import com.lanf.seckill.model.query.SeckillItemPageQuery;
import com.lanf.seckill.model.vo.SeckillItemDetailVO;
import com.lanf.seckill.model.vo.SeckillItemVO;
import com.lanf.seckill.model.vo.SeckillTokenVO;

import java.util.List;

/**
 * <p>
 * 秒杀活动表 服务类
 * </p>
 *
 * @author jarven
 * @since 2026-05-07
 */
public interface ISecKillActivityService extends IService<SecKillActivityDO> {

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

     */
    List<SeckillItemVO> seckillItemPageQuery(SeckillItemPageQuery query);

    /**
     * 查询秒杀商品详情
     * 
     * @param seckillItemId 秒杀商品ID
     * @return 商品详情VO
     */
    SeckillItemDetailVO seckillItemDetailQuery(Long seckillItemId);

    /**
     * 获取秒杀令牌（动态秒杀链接）
     * 秒杀开始时生成一次性 token，用于后续下单验证
     * 
     * @return 秒杀令牌信息
     */
    SeckillTokenVO getSeckillToken( GetSeckillTokenDTO dto);



}
