package com.lanf.seckill.controller.admin;


import com.alibaba.nacos.api.model.v2.Result;
import com.lanf.seckill.model.dto.AddSeckillActivityDTO;
import com.lanf.seckill.model.dto.AddSeckillItemDTO;
import com.lanf.seckill.model.dto.LauncherSeckillItemDTO;
import com.lanf.seckill.service.ISecKillActivityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
@RequestMapping("/admin/seckillActivity")
public class AdminSeckillActivityController {
    @Autowired
    private ISecKillActivityService seckillActivityService;


    /**
     * 添加秒杀活动
     */
    @PostMapping("/add")
    public Result<Void> addSeckillActivity(@Validated @RequestBody AddSeckillActivityDTO dto) {
        log.info("添加秒杀活动: dto={}", dto);


        seckillActivityService.addSeckillActivity(dto);
        return Result.success();

    }

    /**
     * 添加秒杀商品
     */
    @PostMapping("/item/add")
    public Result<Void> addSeckillItem(@Validated @RequestBody AddSeckillItemDTO dto) {
        log.info("添加秒杀商品: dto={}", dto);


        seckillActivityService.addAddSeckillItem(dto);
        return Result.success();

    }

    /**
     * 上架秒杀商品
     */
    @PostMapping("/item/launcher")
    public Result<Void> launcherSeckillItem(@Validated @RequestBody LauncherSeckillItemDTO dto) {
        log.info("上架秒杀商品: seckillItemId={}", dto.getSeckillItemId());

        seckillActivityService.launcherSeckillItem(dto);
        return Result.success();

    }
}

