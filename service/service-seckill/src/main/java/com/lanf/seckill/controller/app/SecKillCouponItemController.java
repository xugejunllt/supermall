package com.lanf.seckill.controller.app;

import com.lanf.cache.service.RedissonCacheService;
import com.lanf.constant.result.Result;
import com.lanf.constant.utils.UserContext;
import com.lanf.seckill.api.SecKillResultCache;
import com.lanf.seckill.mapper.SecKillCouponRecordMapper;
import com.lanf.seckill.model.dto.DeleteCacheDTO;
import com.lanf.seckill.model.dto.GetSecKillCouponTokenDTO;
import com.lanf.seckill.model.enums.SecKillResultEnum;
import com.lanf.seckill.model.vo.SecKillCouponItemVO;
import com.lanf.seckill.model.vo.SecKillCouponTokenVO;
import com.lanf.seckill.service.ISecKillCouponItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 秒杀优惠券项目表 前端控制器（用户端）
 * </p>
 *
 * @author jarven
 * @since 2026-06-20
 */
@Slf4j
@RestController
@RequestMapping("/app/seckillCouponItem")
public class SecKillCouponItemController {

    @Autowired
    private ISecKillCouponItemService seckillCouponItemService;

    @Autowired
    private SecKillResultCache secKillResultCache;
    @Autowired
    private SecKillCouponRecordMapper secKillCouponRecordMapper;
    @Autowired
    private RedissonCacheService redissonCacheService;
    /**
     * 获取秒杀优惠券列表
     */
    @GetMapping("/seckillCouponItemList")
    public Result<List<SecKillCouponItemVO>> seckillCouponItemList() {

        log.info("获取秒杀优惠券列表:");
        List<SecKillCouponItemVO> items = seckillCouponItemService.seckillCouponItemList();
        return Result.ok(items);
    }


    /**
     * 获取秒杀优惠券Token
     */
    @PostMapping("/getSecKillCouponToken")
    public Result<SecKillCouponTokenVO> getSecKillCouponToken(@Validated @RequestBody GetSecKillCouponTokenDTO dto) {
        log.info("获取秒杀优惠券Token: dto={}", dto);
        return Result.ok(seckillCouponItemService.getSecKillCouponToken(dto));
    }

    /**
     * 前端轮询秒杀结果
     */
    @GetMapping("/querySecKillCouponResult")
    public Result<String> querySecKillCouponResult(@RequestParam Long secKillCouponItemId) {
        Long userId = UserContext.getUserId();
        SecKillResultEnum result = secKillResultCache.getResult(userId, secKillCouponItemId);

        String message = null;
        switch (result) {
            case SUCCESS_ORDER_CREATED:
                message = "秒杀成功，优惠券已发放";
                break;
            case SUCCESS_ORDER_CREATING:
                message = "优惠券发放中,请稍后再试";
                break;
            case SOLD_OUT:
                message = "秒杀失败,优惠券已售空";
                break;
        }

        return Result.ok(message);
    }
    @PostMapping("/deleteCache")
    public Result<Void> deleteCache(@RequestBody DeleteCacheDTO dto) {

        log.info("删除用户秒杀缓存记录:{}",dto);
        secKillCouponRecordMapper.deleteAll();
        String participatedKey = String.format("seckill:coupon:user:participated:%s:%s",
                UserContext.getUserId(), dto.getSecKillItemId());
        log.info("删除的缓存key"+participatedKey);
        redissonCacheService.delete(participatedKey);

        return Result.ok();
    }
}
