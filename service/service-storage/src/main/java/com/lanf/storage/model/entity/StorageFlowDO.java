package com.lanf.storage.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 出入库明细
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-07
 */
@Data
@TableName("storage_flow")
public class StorageFlowDO extends BaseEntity {

private static final long serialVersionUID=1L;


    /**
     * 0:销售单出库 1:销售单换货入库 2:销售退货退款入库 3:销售单换货出库 4.采购入库
     */

    private Integer orderType;


    @ApiModelProperty(value = "出入库单code")
    private String bizNumber;

    @ApiModelProperty(value = "仓库名称")
    private String warehousName;

    @ApiModelProperty(value = "出库数量")
    private Integer outQuantity;

    @ApiModelProperty(value = "入库数量")
    private Integer inQuantity;

    //店铺id
    private Long shopId;



}
