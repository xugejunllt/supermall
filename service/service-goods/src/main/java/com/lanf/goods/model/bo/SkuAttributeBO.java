package com.lanf.goods.model.bo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SkuAttributeBO implements Serializable {

    //属性名称
    private String attributeName;


    //属性值
    private List<SkuAttributeDetailBO> attributeValue;


}
