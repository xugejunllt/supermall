package com.lanf.seckill.model.query;

import com.lanf.constant.model.query.PageQuery;
import lombok.Data;

/**
 * <p>
 * 秒杀活动分页查询参数
 * </p>
 *
 * @author jarven
 * @since 2026-05-07
 */
@Data
public class SecKillActivityPageQuery extends PageQuery  {

    private static final long serialVersionUID = 1L;

    /**
     * 活动名称（模糊查询）
     */
    private String name;

}
