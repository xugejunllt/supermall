package com.lanf.goods.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import lombok.Data;

/**
 * <p>
 * 商品同步es记录
 * </p>
 *
 * @author jarven
 * @since 2025-12-05
 */
@Data
@TableName("goods_sync_es_record")
public class GoodsSyncEsRecordDO extends BaseEntity {

private static final long serialVersionUID=1L;



    private Long goodsId;

    /**
     * 当前商品最大版本号
     */
    private Long maxVersion;



}
