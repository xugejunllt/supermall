package com.lanf.search.model.query;

import lombok.Data;
import java.io.Serializable;

@Data
public class GoodsSearchQuery implements Serializable {
    private Integer page = 1;
    private Integer pageSize = 10;
    private String keyword;           // 搜索关键词
    private Long categoryId;          // 分类ID
    private Long brandId;             // 品牌ID
    private Long shopId;
    private String attrName;
    private String attrValue;

    // 店铺ID
    private Integer upDownStatus = 1; // 默认只查上架商品
    private String sortField;         // 排序字段: sales,create_time
    private String sortOrder;         // 排序方式: asc, desc
}
