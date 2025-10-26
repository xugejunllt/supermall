package com.lanf.goods.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SkuNameDTO implements Serializable {

    //属性
    private String attribute;
    //描述
    private String desc;

    private Integer sort;


}
