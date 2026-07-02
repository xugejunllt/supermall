package com.lanf.seckill.model.query;

import com.lanf.constant.model.query.PageQuery;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 秒杀记录分页查询参数
 * </p>
 *
 * @author jarven
 * @since 2026-05-09
 */
@Data
public class SecKillRecordPageQuery extends PageQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 秒杀商品ID
     */
    private Long secKillItemId;

}
