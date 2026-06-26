package com.lanf.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lanf.seckill.model.entity.SecKillCouponRecordDO;
import org.apache.ibatis.annotations.Delete;

/**
 * <p>
 * 秒杀优惠券记录表 Mapper 接口
 * </p>
 *
 * @author jarven
 * @since 2026-06-20
 */
public interface SecKillCouponRecordMapper extends BaseMapper<SecKillCouponRecordDO> {
    @Delete("DELETE FROM sec_kill_coupon_record")
    int deleteAll();
}
