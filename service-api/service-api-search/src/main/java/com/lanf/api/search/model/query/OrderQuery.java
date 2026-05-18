package com.lanf.api.search.model.query;

import lombok.Data;

import java.io.Serializable;

@Data
public class OrderQuery implements Serializable {

    private String orderNumber;

    // 租户id
    private Long tenantId;

    private Integer orderStatus;
    /**
     * 搜索词
     */
    private String searchWord;
}
