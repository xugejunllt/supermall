package com.lanf.search.model.query;

import lombok.Data;

import java.io.Serializable;

@Data
public class GoodsPageVO implements Serializable {

    private String id;
    private Long goodsId;
    //商品编码
    private String code;
    //商品名称
    private String name ;
    //搜索词
    private String searchWords;

    //上下架状态 0:上架 ,1:下架"
    private Integer upDownStatus;
    //商品价格
    private Double price;
    //图片
    private String picture;

    private Long createTime;

    private Long updateTime;
}
