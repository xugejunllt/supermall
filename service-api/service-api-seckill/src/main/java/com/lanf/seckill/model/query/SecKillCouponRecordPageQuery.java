package com.lanf.seckill.model.query;

import com.lanf.constant.model.query.PageQuery;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 秒杀优惠券记录分页查询参数
 * </p>
 *
 * @author jarven
 * @since 2026-06-20
 */
@Data
public class SecKillCouponRecordPageQuery extends PageQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 秒杀优惠券项目ID
     */
    private Long secKillCouponItemId;

    /**
     * 状态：0-秒杀成功，1-优惠券已发放，2-秒杀失败
     */
    private Integer status;

}
