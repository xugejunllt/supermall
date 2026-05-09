package com.lanf.search.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class HomePageVO implements Serializable {


    //商品id
    private Long goodsId;
    //商品名称
    private String goodsName;

    //商品主图
    private String mainImage;

    // sku价格
    private Double price;

    //扩展标签 用于展示
    private List<String> extendedTags;
    //商品销量
    private Long sales;
}
