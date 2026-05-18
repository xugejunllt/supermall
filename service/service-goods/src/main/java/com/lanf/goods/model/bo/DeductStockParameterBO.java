package com.lanf.goods.model.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class DeductStockParameterBO implements Serializable {

    private Long tenantId;

    private String skuCode;

    private String orderNumber;

    private Long stockId;
}
