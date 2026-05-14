package com.lanf.storage.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.constant.model.enums.storage.PublishPlatformEnum;
import com.lanf.constant.model.enums.storage.PublishStatusEnum;
import com.lanf.constant.model.enums.storage.StockPreorderEventTypeEnum;
import lombok.Data;

/**
 * <p>
 * 库存预售发布记录
 * </p>
 *
 * @author jarven
 * @since 2026-05-05
 */
@Data
@TableName("stock_preorder_publish_log")
public class StockPreorderPublishLogDO extends BaseEntity {

private static final long serialVersionUID=1L;



    private String flowNo;

    /** 库存id */
    private Long stockId;

    /** sku编码 */
    private String skuCode;

    private Integer changeQuantity;

    private StockPreorderEventTypeEnum eventType;

    private PublishPlatformEnum publishPlatform;

    private Long warehouseId;

    private PublishStatusEnum status;

    private Long  tenantId;
    /** 仓库名称 */
    private String warehouseName;
}
