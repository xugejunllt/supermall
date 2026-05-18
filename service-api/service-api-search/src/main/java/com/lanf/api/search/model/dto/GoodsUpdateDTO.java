package com.lanf.api.search.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class GoodsUpdateDTO implements Serializable {

    private Long goodsId;
    //商品编码
    private String code;
    //商品名称
    private String name ;
    //上下架状态 0:上架 ,1:下架"
    private Integer upDownStatus;
    //商品价格
    private BigDecimal price;
    //图片
    private String picture;

    private Date createTime;

    private Date updateTime;
    //搜索词
    private String searchWords;
}
