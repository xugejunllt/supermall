package com.lanf.seckill.model.query;

import com.lanf.constant.model.query.PageQuery;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 秒杀优惠券项目分页查询参数
 * </p>
 *
 * @author jarven
 * @since 2026-06-20
 */
@Data
public class SecKillCouponItemPageQuery extends PageQuery implements Serializable {

    private static final long serialVersionUID = 1L;



    /**
     * 上架状态：0-下架，1-上架
     */
    private Integer shelfStatus;

}
