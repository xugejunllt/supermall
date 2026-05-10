package com.lanf.order.model.vo;

import com.lanf.constant.enums.order.OrderStatusEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class OrderDocumentVO implements Serializable {


    private Long orderId;
    // 用户id (通常作为分片键或关联查询键)
    private Long userId;

    private String orderNumber;

    // 租户id
    private Long tenantId;

    private OrderStatusEnum orderStatus;

    private Date createTime;


    private List<String> goodsNames;

}
