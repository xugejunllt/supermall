package com.lanf.goods.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class BaseGoodsSkuVO implements Serializable {

    private String skuCode;

    private String skuName;

    private String skuPictureAddress;
}
