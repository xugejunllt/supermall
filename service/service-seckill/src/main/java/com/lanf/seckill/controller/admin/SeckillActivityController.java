package com.lanf.seckill.controller.admin;


import com.alibaba.nacos.api.model.v2.Result;
import com.lanf.seckill.model.vo.SeckillItemDetailVO;
import com.lanf.seckill.model.vo.SeckillItemVO;
import com.lanf.seckill.service.ISeckillActivityService;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/seckillActivity")
public class SeckillActivityController {
    @Autowired
    private ISeckillActivityService seckillActivityService;

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
}

