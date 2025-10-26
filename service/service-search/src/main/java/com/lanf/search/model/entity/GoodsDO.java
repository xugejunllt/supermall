package com.lanf.search.model.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Data
@Document(indexName = "goods_index") // 指定索引名称
public class GoodsDO {

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