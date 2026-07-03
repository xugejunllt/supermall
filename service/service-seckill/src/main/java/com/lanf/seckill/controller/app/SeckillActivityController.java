package com.lanf.seckill.controller.app;


import com.lanf.cache.service.RedissonCacheService;
import com.lanf.constant.result.Result;
import com.lanf.constant.utils.UserContext;
import com.lanf.seckill.mapper.SecKillRecordMapper;
import com.lanf.seckill.model.dto.DeleteCacheDTO;
import com.lanf.seckill.model.dto.GetSeckillTokenDTO;
import com.lanf.seckill.model.query.SecKillResultQuery;
import com.lanf.seckill.model.query.SeckillItemPageQuery;
import com.lanf.seckill.model.vo.*;
import com.lanf.seckill.service.ISecKillActivityService;
import com.lanf.seckill.model.enums.SecKillResultEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * <p>
 * 秒杀活动表 前端控制器
 * </p>
 *
 * @author jarven
 * @since 2026-05-07
 */
@Slf4j
@RestController
@RequestMapping("/app/seckillActivity")
public class SeckillActivityController {


    @Autowired
    private ISecKillActivityService seckillActivityService;
    @Autowired
    private SecKillResultCache secKillResultCache;

    @Autowired
    private RedissonCacheService redissonCacheService;
    @Autowired
    private SecKillRecordMapper secKillRecordMapper;
    /**
     * 获取秒杀商品列表
     */
    @GetMapping("/seckillItemPageQuery")
    public Result<List<SeckillItemVO>> seckillItemPageQuery(
            SeckillItemPageQuery query) {
        log.info("分页查询秒杀商品列表:{}", query);
        List<SeckillItemVO> items = seckillActivityService.seckillItemPageQuery(query);
        return Result.ok(items);
    }

    /**
     * 查询商品详情
     */
    @GetMapping("/seckillItemDetailQuery")
    public Result<SeckillItemDetailVO> seckillItemDetailQuery(Long seckillItemId) {

        log.info("查询秒杀商品详细:{}", seckillItemId);
        SeckillItemDetailVO detail = seckillActivityService.seckillItemDetailQuery(seckillItemId);


        return Result.ok(detail);
    }

    @PostMapping("/getSeckillToken")
    public Result<SeckillTokenVO> getSeckillToken(@Validated @RequestBody GetSeckillTokenDTO dto) {


        return Result.ok(seckillActivityService.getSeckillToken(dto));
    }


    /**
     * 前端轮训秒杀结果
     */
    @GetMapping("/querySecKillResult")
    public Result<SecKillResultVO> querySecKillResult(SecKillResultQuery query) {

        Long userId = UserContext.getUserId();
        SecKillResultEnum result = secKillResultCache.getResult(userId, query.getSecKillItemId());

        /**
         * 返回友好提示
         */
        String message = null;
        switch (result) {

            case SUCCESS_ORDER_CREATED:
                message = "秒杀成功，订单生成完成,请前往订单列表页查询并支付";
                break;
            case SUCCESS_ORDER_CREATING:
                message = "订单生成中,请稍后再试";
                break;
            case SOLD_OUT:
                message = "秒杀失败,商品已售空";
                break;

        }
        SecKillResultVO vo = new SecKillResultVO();
        vo.setMessage(message);
        return Result.ok(vo);
    }

    /**
     * 查询秒杀活动列表
     */
    @GetMapping("/seckillActivityListQuery")
    public Result<List<SecKillActivityListVO>> seckillActivityListQuery() {

        log.info("查询秒杀活动列表");

        return Result.ok(seckillActivityService.seckillActivityListQuery());
    }
    @PostMapping("/deleteCache")
    public Result<Void> deleteCache(@RequestBody DeleteCacheDTO dto) {

        log.info("删除用户秒杀缓存记录:{}",dto);
        secKillRecordMapper.deleteAll();
        String participatedKey = String.format("seckill:user:participated:%s:%s",
                UserContext.getUserId(), dto.getSecKillItemId());
        log.info("删除的缓存key"+participatedKey);
        redissonCacheService.delete(participatedKey);

        return Result.ok();
    }

}

