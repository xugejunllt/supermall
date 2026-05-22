// D:\item2\mail\service\service-order\src\main\java\com\lanf\order\model\entity\ShippingInfoDO.java
package com.lanf.order.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import lombok.Data;

/**
 * <p>
 * 物流信息表
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-17
 */
@Data
@TableName("shipping_info")
public class ShippingInfoDO extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 订单id
     */
    private Long orderId;

    private Long userId;

    /**
     * 物流公司名称
     */
    private String logisticsCompany;

    /**
     * 快递公司编码
     */
    private String logisticsCode;

    /**
     * 物流单号
     */
    private String trackingNumber;

    /**
     * 发货地址
     */
    private String fromAddress;

    private Long tenantId;
}
