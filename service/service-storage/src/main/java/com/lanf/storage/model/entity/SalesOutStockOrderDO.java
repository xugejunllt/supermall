package com.lanf.storage.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.api.storage.model.enums.StorageStatusEnum;
import lombok.Data;

/**
 * <p>
 * 销售出库单
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-09
 */
@Data
@TableName("sales_out_stock_order")
public class SalesOutStockOrderDO extends BaseEntity {

private static final long serialVersionUID=1L;


    private Long userId;
    /** 单据编码 */
    private String code;

    /** 订单id */
    private Long orderId;

    private StorageStatusEnum storageStatus;

    private Long version;
    private Long  tenantId;


}
