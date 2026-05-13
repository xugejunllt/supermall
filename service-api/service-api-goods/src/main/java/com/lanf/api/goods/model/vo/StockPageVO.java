package com.lanf.api.goods.model.vo;


import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 库存分页VO
 */
@Data
public class StockPageVO implements Serializable {

    /** ID */
    private Long id;

    /** SKU编码 */
    private String skuCode;

    /** 商品名称 */
    private String goodsName;

    /** 单位 */
    private String unit;

    /** 可使用库存 */
    private Integer usableStock;

    /** 锁住的库存 */
    private Integer lockStock;

    /** 仓库ID */
    private Long warehouseId;

    /** 仓库名称 */
    private String warehouseName;

    /** 版本号 */
    private Long version;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;

}
