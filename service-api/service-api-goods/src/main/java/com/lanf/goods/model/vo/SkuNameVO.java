package com.lanf.goods.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * SKU名称VO
 */
@Data
public class SkuNameVO implements Serializable {

    /** 属性 */
    private String attribute;
    
    /** 描述 */
    private String desc;

    private Integer sort;

}
