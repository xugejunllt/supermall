package com.lanf.seckill.controller.app;


import com.alibaba.nacos.api.model.v2.Result;
import com.lanf.seckill.model.dto.GetSeckillTokenDTO;
import com.lanf.seckill.model.enums.SecKillResultEnum;
import com.lanf.seckill.model.query.SecKillResultQuery;
import com.lanf.seckill.model.vo.SecKillResultVO;
import com.lanf.seckill.model.vo.SeckillItemDetailVO;
import com.lanf.seckill.model.vo.SeckillItemVO;
import com.lanf.seckill.model.vo.SeckillTokenVO;
import com.lanf.seckill.service.ISecKillActivityService;
import com.lanf.seckill.service.strategy.SecKillResultCache;
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
@RestController
@RequestMapping("/app/seckillActivity")
public class SeckillActivityController {


    @Autowired
    private ISecKillActivityService seckillActivityService;
    @Autowired
    private SecKillResultCache secKillResultCache;

    /**
     *
     * 获取秒杀商品列表
     *
     */
    @GetMapping("/items/{activityId}")
    public Result<List<SeckillItemVO>> getSeckillItems(
            @PathVariable Long activityId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {

        List<SeckillItemVO> items = seckillActivityService.pageQuerySeckillItems(activityId, pageNum, pageSize);
        return Result.success(items);
    }
    /**
     * 查询商品详情
     */
    @GetMapping("/item/detail/{seckillItemId}")
    public Result<SeckillItemDetailVO> getItemDetail(@PathVariable Long seckillItemId) {

        SeckillItemDetailVO detail = seckillActivityService.getSeckillItemDetail( seckillItemId);


        return Result.success(detail);
    }

    @PostMapping("/item/detail/getSeckillToken")
    public Result<SeckillTokenVO> getSeckillToken(@Validated @RequestBody GetSeckillTokenDTO dto) {


        return Result.success(seckillActivityService.getSeckillToken( dto));
    }

    /**
     * 前端轮训秒杀结果
     *
     *
     */
    @GetMapping("/querySecKillResult")
    public Result<SecKillResultVO> querySecKillResult(SecKillResultQuery query) {

        Long userId = UserIdContext.getUserId() ;
        SecKillResultEnum result = secKillResultCache.getResult(userId, query.getSecKillItemId());
        /**
         * 返回友好提示
         */
        String message = null;
        switch ( result){

            case SUCCESS_ORDER_CREATED:
                 message = "秒杀成功，订单生成完成,请前往订单列表页查询并支付";
               break;
            case SUCCESS_ORDER_CREATING:
                message = "订单生成中,请稍后再试";
                break;
            case FAILED:
                message = "系统繁忙,请联系客服处理";
                break;
            case SOLD_OUT:
                message = "秒杀失败,商品已售空";
                break;

        }
        SecKillResultVO vo = new SecKillResultVO();
        vo.setMessage(message);
        return Result.success(vo);
    }

}

