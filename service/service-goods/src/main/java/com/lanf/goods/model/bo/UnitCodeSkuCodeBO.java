package com.lanf.goods.model.bo;

import lombok.Data;

import java.io.Serializable;

/**
 * 属性code -skuCde映射
 */
@Data
public class UnitCodeSkuCodeBO implements Serializable {


    private String unitCode;

    private String skuCode;
}
