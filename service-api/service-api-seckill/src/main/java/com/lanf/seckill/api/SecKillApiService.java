package com.lanf.seckill.api;

import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import com.lanf.seckill.model.query.SecKillActivityPageQuery;
import com.lanf.seckill.model.query.SecKillCouponItemPageQuery;
import com.lanf.seckill.model.query.SecKillCouponRecordPageQuery;
import com.lanf.seckill.model.query.SecKillItemPageQuery;
import com.lanf.seckill.model.query.SecKillRecordPageQuery;
import com.lanf.seckill.model.vo.SecKillActivityPageVO;
import com.lanf.seckill.model.vo.SecKillCouponItemPageVO;
import com.lanf.seckill.model.vo.SecKillCouponRecordPageVO;
import com.lanf.seckill.model.vo.SecKillItemPageVO;
import com.lanf.seckill.model.vo.SecKillRecordPageVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Component
@FeignClient(name = "service-seckill")
public interface SecKillApiService {

    /**
     * 分页查询秒杀活动列表
     */
    @GetMapping("/seckill/admin/seckillActivity/seckillActivityPageQuery")
    Result<PageResult<SecKillActivityPageVO>> seckillActivityPageQuery(@RequestParam SecKillActivityPageQuery query);

    /**
     * 分页查询秒杀商品列表
     */
    @GetMapping("/seckill/admin/seckillActivity/seckillItemPageQuery")
    Result<PageResult<SecKillItemPageVO>> seckillItemPageQuery(@RequestParam SecKillItemPageQuery query);

    /**
     * 分页查询秒杀记录列表
     */
    @GetMapping("/seckill/admin/seckillActivity/seckillRecordPageQuery")
    Result<PageResult<SecKillRecordPageVO>> seckillRecordPageQuery(@RequestParam SecKillRecordPageQuery query);

    /**
     * 分页查询秒杀优惠券项目列表
     */
    @GetMapping("/seckill/admin/seckillCouponItem/seckillCouponItemPageQuery")
    Result<PageResult<SecKillCouponItemPageVO>> seckillCouponItemPageQuery(@RequestParam SecKillCouponItemPageQuery query);

    /**
     * 分页查询秒杀优惠券记录列表
     */
    @GetMapping("/seckill/admin/seckillCouponItem/seckillCouponRecordPageQuery")
    Result<PageResult<SecKillCouponRecordPageVO>> seckillCouponRecordPageQuery(@RequestParam SecKillCouponRecordPageQuery query);

}
