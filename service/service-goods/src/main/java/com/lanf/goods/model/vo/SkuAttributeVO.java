package com.lanf.goods.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

@Data
public class SkuAttributeVO implements Serializable {

    //属性名称
    private String attributeName;
    //属性值
    private List<String> attributeValue;


}
