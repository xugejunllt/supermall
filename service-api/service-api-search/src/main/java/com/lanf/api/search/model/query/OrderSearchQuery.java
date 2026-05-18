package com.lanf.api.search.model.query;

import lombok.Data;

import java.io.Serializable;

@Data
public class OrderSearchQuery implements Serializable {


    private String orderNumber;

    private Long tenantId;

    private Integer orderStatus;

    private String searchWord;

    private Long userId;

    protected long page = 1;

    protected long pageSize = 20;
}
