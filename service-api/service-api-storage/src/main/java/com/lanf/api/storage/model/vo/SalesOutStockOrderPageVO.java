package com.lanf.api.storage.model.vo;

import com.lanf.api.storage.model.enums.StorageStatusEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class SalesOutStockOrderPageVO implements Serializable {

    private Long id;
    /** 单据编码 */
    private String code;

    /** 订单id */
    private Long orderId;

    private StorageStatusEnum storageStatus;

    private Date createTime;
}
