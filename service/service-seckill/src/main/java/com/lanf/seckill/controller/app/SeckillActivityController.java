package com.lanf.seckill.controller.app;


import com.alibaba.nacos.api.model.v2.Result;
import com.lanf.seckill.model.dto.GetSeckillTokenDTO;
import com.lanf.seckill.model.vo.SeckillItemDetailVO;
import com.lanf.seckill.model.vo.SeckillItemVO;
import com.lanf.seckill.model.vo.SeckillTokenVO;
import com.lanf.seckill.service.ISeckillActivityService;
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
    private ISeckillActivityService seckillActivityService;

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
}

