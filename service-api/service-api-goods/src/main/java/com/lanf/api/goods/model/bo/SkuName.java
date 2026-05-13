package com.lanf.api.goods.model.bo;

import lombok.Data;

import java.io.Serializable;

/**
 * SKU名称BO
 */
@Data
public class SkuName implements Serializable {

    /** 属性 */
    private String attribute;
    
    /** 描述 */
    private String desc;

    private Integer sort;
    
    /** 唯一id */
    private Long unitId;

    /** 0:没有选中 ,1:默认选中 */
    private Integer defaultSelect;
}
