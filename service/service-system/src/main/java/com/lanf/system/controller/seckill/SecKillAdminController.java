package com.lanf.system.controller.seckill;

import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import com.lanf.seckill.api.SecKillApiService;
import com.lanf.seckill.model.dto.AddSecKillCouponItemDTO;
import com.lanf.seckill.model.dto.AddSeckillActivityDTO;
import com.lanf.seckill.model.dto.AddSeckillItemDTO;
import com.lanf.seckill.model.dto.LauncherSecKillCouponItemDTO;
import com.lanf.seckill.model.dto.LauncherSeckillItemDTO;
import com.lanf.seckill.model.query.SecKillActivityPageQuery;
import com.lanf.seckill.model.query.SecKillCouponItemPageQuery;
import com.lanf.seckill.model.query.SecKillCouponRecordPageQuery;
import com.lanf.seckill.model.query.SecKillItemForAdminPageQuery;
import com.lanf.seckill.model.query.SecKillRecordPageQuery;
import com.lanf.seckill.model.vo.SecKillActivityPageVO;
import com.lanf.seckill.model.vo.SecKillCouponItemPageVO;
import com.lanf.seckill.model.vo.SecKillCouponRecordPageVO;
import com.lanf.seckill.model.vo.SecKillItemPageVO;
import com.lanf.seckill.model.vo.SecKillRecordPageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/secKill")
public class SecKillAdminController {

    @Autowired
    private SecKillApiService secKillApiService;

    /**
     * 分页查询秒杀活动列表
     */
    @GetMapping("/seckillActivityPageQuery")
    public Result<PageResult<SecKillActivityPageVO>> seckillActivityPageQuery(@Validated SecKillActivityPageQuery query) {
        log.info("分页查询秒杀活动列表: query={}", query);
        return secKillApiService.seckillActivityPageQuery(query);
    }

    /**
     * 分页查询秒杀商品列表
     */
    @GetMapping("/seckillItemPageQuery")
    public Result<PageResult<SecKillItemPageVO>> seckillItemPageQuery(@Validated SecKillItemForAdminPageQuery query) {
        log.info("分页查询秒杀商品列表: query={}", query);
        return secKillApiService.seckillItemPageQuery(query);
    }

    /**
     * 分页查询秒杀记录列表
     */
    @GetMapping("/seckillRecordPageQuery")
    public Result<PageResult<SecKillRecordPageVO>> seckillRecordPageQuery(@Validated SecKillRecordPageQuery query) {
        log.info("分页查询秒杀记录列表: query={}", query);
        return secKillApiService.seckillRecordPageQuery(query);
    }

    /**
     * 分页查询秒杀优惠券项目列表
     */
    @GetMapping("/seckillCouponItemPageQuery")
    public Result<PageResult<SecKillCouponItemPageVO>> seckillCouponItemPageQuery(@Validated SecKillCouponItemPageQuery query) {
        log.info("分页查询秒杀优惠券项目列表: query={}", query);
        return secKillApiService.seckillCouponItemPageQuery(query);
    }

    /**
     * 分页查询秒杀优惠券记录列表
     */
    @GetMapping("/seckillCouponRecordPageQuery")
    public Result<PageResult<SecKillCouponRecordPageVO>> seckillCouponRecordPageQuery(@Validated SecKillCouponRecordPageQuery query) {
        log.info("分页查询秒杀优惠券记录列表: query={}", query);
        return secKillApiService.seckillCouponRecordPageQuery(query);
    }

    /**
     * 添加秒杀活动
     */
    @PostMapping("/addSeckillActivity")
    public Result<Void> addSeckillActivity(@Validated @RequestBody AddSeckillActivityDTO dto) {
        log.info("添加秒杀活动: dto={}", dto);
        return secKillApiService.addSeckillActivity(dto);
    }

    /**
     * 添加秒杀商品
     */
    @PostMapping("/addSeckillItem")
    public Result<Void> addSeckillItem(@Validated @RequestBody AddSeckillItemDTO dto) {
        log.info("添加秒杀商品: dto={}", dto);
        return secKillApiService.addSeckillItem(dto);
    }

    /**
     * 上架秒杀商品
     */
    @PostMapping("/launcherSeckillItem")
    public Result<Void> launcherSeckillItem(@Validated @RequestBody LauncherSeckillItemDTO dto) {
        log.info("上架秒杀商品: dto={}", dto);
        return secKillApiService.launcherSeckillItem(dto);
    }

    /**
     * 添加秒杀优惠券
     */
    @PostMapping("/addSecKillCouponItem")
    public Result<Void> addSecKillCouponItem(@Validated @RequestBody AddSecKillCouponItemDTO dto) {
        log.info("添加秒杀优惠券: dto={}", dto);
        return secKillApiService.addSecKillCouponItem(dto);
    }

    /**
     * 上架秒杀优惠券
     */
    @PostMapping("/launcherSecKillCouponItem")
    public Result<Void> launcherSecKillCouponItem(@Validated @RequestBody LauncherSecKillCouponItemDTO dto) {
        log.info("上架秒杀优惠券: dto={}", dto);
        return secKillApiService.launcherSecKillCouponItem(dto);
    }

}
