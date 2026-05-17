package com.lanf.goods.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import lombok.Data;

import java.math.BigDecimal;

/**
 * <p>
 * 库存
 * </p>
 *
 * @author jarven
 * @since 2025-11-29
 */
@Data
@TableName("user_stock")
public class StockDO extends BaseEntity {

private static final long serialVersionUID=1L;


    /** sku编码 */
    private String skuCode;

    private Long goodsId;

    /** 可使用库存 */
    private Integer usableStock;

    /** 锁住的库存 */
    private Integer lockStock;

    /** 仓库id */
    private Long warehouseId;

    /** 仓库名称 */
    private String warehouseName;

    private String areaCode;

    private BigDecimal latitude;
    /**
     * 经度
     */
    private BigDecimal longitude;

    /** 版本号 乐观锁控制 */
    private Long version;
    
    private Long tenantId;
}
