package com.lanf.storage.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class StockPageQueryVO implements Serializable {

    private Long id;
    /** sku编码 */
    private String skuCode;

    /** 总库存 */
    private Integer totalStock;

    /** 锁住的库存 */
    private Integer lockStock;

    /** 可用库存 */
    private Integer usableStock;
    //商品单位
    private String unit;
    /** 仓库名称 */
    private String warehouseName;

    private String goodsName;

    private Date createTime;
}
