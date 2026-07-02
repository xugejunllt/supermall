package com.lanf.seckill.controller.admin;

import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import com.lanf.seckill.model.dto.AddSecKillCouponItemDTO;
import com.lanf.seckill.model.dto.LauncherSecKillCouponItemDTO;
import com.lanf.seckill.service.ISecKillCouponItemService;
import com.lanf.seckill.service.ISecKillCouponRecordService;
import com.lanf.seckill.model.query.SecKillCouponItemPageQuery;
import com.lanf.seckill.model.query.SecKillCouponRecordPageQuery;
import com.lanf.seckill.model.vo.SecKillCouponItemPageVO;
import com.lanf.seckill.model.vo.SecKillCouponRecordPageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    @Autowired
    private ISecKillCouponRecordService seckillCouponRecordService;

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

    /**
     * 分页查询秒杀优惠券项目列表
     */
    @GetMapping("/seckillCouponItemPageQuery")
    public Result<PageResult<SecKillCouponItemPageVO>> seckillCouponItemPageQuery(@Validated SecKillCouponItemPageQuery query) {
        log.info("分页查询秒杀优惠券项目列表: query={}", query);
        return Result.ok(seckillCouponItemService.seckillCouponItemPageQuery(query));
    }

    /**
     * 分页查询秒杀优惠券记录列表
     */
    @GetMapping("/seckillCouponRecordPageQuery")
    public Result<PageResult<SecKillCouponRecordPageVO>> seckillCouponRecordPageQuery(@Validated SecKillCouponRecordPageQuery query) {
        log.info("分页查询秒杀优惠券记录列表: query={}", query);
        return Result.ok(seckillCouponRecordService.seckillCouponRecordPageQuery(query));
    }

}
