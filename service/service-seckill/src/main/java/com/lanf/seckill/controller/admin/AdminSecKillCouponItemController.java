package com.lanf.seckill.controller.admin;

import com.lanf.constant.result.Result;
import com.lanf.seckill.model.dto.AddSecKillCouponItemDTO;
import com.lanf.seckill.model.dto.LauncherSecKillCouponItemDTO;
import com.lanf.seckill.service.ISecKillCouponItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 秒杀优惠券项目表 前端控制器（管理端）
 * </p>
 *
 * @author jarven
 * @since 2026-06-20
 */
@Slf4j
@RestController
@RequestMapping("/admin/seckillCouponItem")
public class AdminSecKillCouponItemController {

    @Autowired
    private ISecKillCouponItemService seckillCouponItemService;

    /**
     * 添加秒杀优惠券
     */
    @PostMapping("/addSecKillCouponItem")
    public Result<Void> addSecKillCouponItem(@Validated @RequestBody AddSecKillCouponItemDTO dto) {
        log.info("添加秒杀优惠券: dto={}", dto);

        seckillCouponItemService.addSecKillCouponItem(dto);
        return Result.ok();
    }

    /**
     * 上架秒杀优惠券
     */
    @PostMapping("/launcherSecKillCouponItem")
    public Result<Void> launcherSecKillCouponItem(@Validated @RequestBody LauncherSecKillCouponItemDTO dto) {
        log.info("上架秒杀优惠券: dto={}", dto);

        seckillCouponItemService.launcherSecKillCouponItem(dto);
        return Result.ok();
    }

}
