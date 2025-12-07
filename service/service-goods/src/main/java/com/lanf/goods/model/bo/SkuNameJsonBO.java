package com.lanf.goods.model.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class SkuNameJsonBO implements Serializable {

    //属性
    private String attribute;
    //描述
    private String desc;

    private Integer sort;
    //唯一id
    private Long unitId;
}
