package com.lanf.api.goods.model.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class Attributes implements Serializable {

    private String attribute;

    private String attributeValue;
}
