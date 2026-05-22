package com.lanf.order.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import lombok.Data;

import java.util.Date;


@Data
@TableName("shipping_track")
public class ShippingTrackDO extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 订单id
     */
    private Long orderId;

    private Long userId;

    /**
     * 物流状态
     */
    private Integer status;

    /**
     * 三方物流基础轨迹状态
     */
    private Integer baseTrackStatus;

    /**
     * 三方物流高级轨迹状态
     */
    private Integer advancedTrackStatus;

    /**
     * 当前完成时间
     */
    private Date finishTime;

    /**
     * 完成内容
     */
    private String finishContent;

    private Long tenantId;
}
