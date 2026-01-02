package com.lanf.goods.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 与仓储库存同步记录
 * </p>
 *
 * @author jarven
 * @since 2025-11-29
 */
@Data
@TableName("user_stock_sync_record")
public class UserStockSyncRecordDO extends BaseEntity {

private static final long serialVersionUID=1L;


    private Long userStockId;
    /**
     * 仓储库存id
     */
    private Long stockFlowId;

    @ApiModelProperty(value = "0:采购入库")
    private Integer orderType;

    @ApiModelProperty(value = "商品sku编码")
    private String skuCode;

    /**
     * 变更前的数量
     */
    private Integer beforeQuantity;
    /**
     *  变更后的数量
     */
    private Integer afterQuantity;

    @ApiModelProperty(value = "出库数量")
    private Integer outQuantity;

    @ApiModelProperty(value = "入库数量")
    private Integer inQuantity;

    private Long warehouseId;

    @ApiModelProperty(value = "仓库名称")
    private String warehouseName;






}
