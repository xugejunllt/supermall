package com.lanf.goods.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 属性code -skuCde映射
 */
@Data
public class UnitCodeSkuCodeVO implements Serializable {


    private String unitCode;

    private String skuCode;
}
