package com.lanf.storage.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import lombok.Data;

/**
 * <p>
 * 
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-07
 */
@Data
@TableName("stock")
public class StockDO extends BaseEntity {

private static final long serialVersionUID=1L;

    //`sku_code`, `warehouse_id` 唯一索引 用于并发控制

    /** sku编码 */
    private String skuCode;

    private String goodsName;

    /** 仓库名称 */
    private String warehouseName;

    //单位
    private String unit;

    /**
     * 可使用库存
     */
    private Integer usableStock;

    /**
     * 预售库存
     */
    private Integer preStock;



    //版本号 乐观锁控制
    private Long version;

    /** 仓库id */
    private Long warehouseId;


    private Long  tenantId;

}
