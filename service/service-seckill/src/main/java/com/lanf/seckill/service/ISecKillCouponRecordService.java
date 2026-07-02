package com.lanf.seckill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.seckill.model.entity.SecKillCouponRecordDO;
import com.lanf.seckill.model.query.SecKillCouponRecordPageQuery;
import com.lanf.seckill.model.vo.SecKillCouponRecordPageVO;

/**
 * <p>
 * 秒杀优惠券记录表 服务类
 * </p>
 *
 * @author jarven
 * @since 2026-06-20
 */
public interface ISecKillCouponRecordService extends IService<SecKillCouponRecordDO> {

    /**
     * 分页查询秒杀优惠券记录列表
     *
     * @param query 分页查询参数
     * @return 分页查询结果
     */
    PageResult<SecKillCouponRecordPageVO> seckillCouponRecordPageQuery(SecKillCouponRecordPageQuery query);

}
