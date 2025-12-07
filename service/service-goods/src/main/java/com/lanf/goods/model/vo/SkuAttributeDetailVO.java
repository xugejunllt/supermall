package com.lanf.goods.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class SkuAttributeDetailVO implements Serializable {

    //属性值
    private String desc;

    //0:没有选中 ,1:默认选中
    private Integer defaultSelect;

    private Long unitId;
}
