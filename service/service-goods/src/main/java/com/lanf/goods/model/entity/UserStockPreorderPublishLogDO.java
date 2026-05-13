package com.lanf.goods.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.constant.model.enums.storage.PublishPlatformEnum;
import com.lanf.storage.model.enums.PublishStatusEnum;
import com.lanf.storage.model.enums.StockPreorderEventTypeEnum;
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
@TableName("user_stock_preorder_publish_log")
public class UserStockPreorderPublishLogDO extends BaseEntity {

private static final long serialVersionUID=1L;

    private String flowNo;

    /** 库存id */
    private Long stockId;

    /** sku编码 */
    private String skuCode;

    private Integer changeQuantity;

    private StockPreorderEventTypeEnum eventType;

    /** 发布平台，支持多商城。0：mail商城 */
    private PublishPlatformEnum publishPlatform;

    private Long warehouseId;

    /** 商家id */
    private Long tenantId;

    private PublishStatusEnum status;

}
