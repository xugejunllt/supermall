package com.lanf.api.goods.model.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class AttributesJson implements Serializable {

    private String attribute;

    private String attributeValue;
}
