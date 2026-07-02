package com.lanf.seckill.controller.admin;


import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import com.lanf.seckill.model.dto.AddSeckillActivityDTO;
import com.lanf.seckill.model.dto.AddSeckillItemDTO;
import com.lanf.seckill.model.dto.LauncherSeckillItemDTO;
import com.lanf.seckill.service.ISecKillActivityService;
import com.lanf.seckill.service.ISecKillItemService;
import com.lanf.seckill.service.ISecKillRecordService;
import com.lanf.seckill.model.query.SecKillActivityPageQuery;
import com.lanf.seckill.model.query.SecKillItemPageQuery;
import com.lanf.seckill.model.query.SecKillRecordPageQuery;
import com.lanf.seckill.model.vo.SecKillActivityPageVO;
import com.lanf.seckill.model.vo.SecKillItemPageVO;
import com.lanf.seckill.model.vo.SecKillRecordPageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    @Autowired
    private ISecKillItemService seckillItemService;

    @Autowired
    private ISecKillRecordService seckillRecordService;


    /**
     * 添加秒杀活动
     */
    @PostMapping("/addSeckillActivity")
    public Result<Void> addSeckillActivity(@Validated @RequestBody AddSeckillActivityDTO dto) {
        log.info("添加秒杀活动: dto={}", dto);


        seckillActivityService.addSeckillActivity(dto);
        return Result.ok();

    }

    /**
     * 添加秒杀商品
     */
    @PostMapping("/addSeckillItem")
    public Result<Void> addSeckillItem(@Validated @RequestBody AddSeckillItemDTO dto) {
        log.info("添加秒杀商品: dto={}", dto);


        seckillActivityService.addSeckillItem(dto);
        return Result.ok();

    }

    /**
     * 上架秒杀商品
     */
    @PostMapping("/launcherSeckillItem")
    public Result<Void> launcherSeckillItem(@Validated @RequestBody LauncherSeckillItemDTO dto) {
        log.info("上架秒杀商品: seckillItemId={}", dto.getSeckillItemId());

        seckillActivityService.launcherSeckillItem(dto);
        return Result.ok();

    }

    /**
     * 分页查询秒杀活动列表
     */
    @GetMapping("/seckillActivityPageQuery")
    public Result<PageResult<SecKillActivityPageVO>> seckillActivityPageQuery(@Validated  SecKillActivityPageQuery query) {
        log.info("分页查询秒杀活动列表: query={}", query);
        return Result.ok(seckillActivityService.seckillActivityPageQuery(query));
    }

    /**
     * 分页查询秒杀商品列表
     */
    @GetMapping("/seckillItemPageQuery")
    public Result<PageResult<SecKillItemPageVO>> seckillItemPageQuery(@Validated SecKillItemPageQuery query) {
        log.info("分页查询秒杀商品列表: query={}", query);
        return Result.ok(seckillItemService.seckillItemPageQuery(query));
    }

    /**
     * 分页查询秒杀记录列表
     */
    @GetMapping("/seckillRecordPageQuery")
    public Result<PageResult<SecKillRecordPageVO>> seckillRecordPageQuery(@Validated SecKillRecordPageQuery query) {
        log.info("分页查询秒杀记录列表: query={}", query);
        return Result.ok(seckillRecordService.seckillRecordPageQuery(query));
    }
}

