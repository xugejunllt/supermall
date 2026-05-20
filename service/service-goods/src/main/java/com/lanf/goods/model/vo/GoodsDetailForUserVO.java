package com.lanf.goods.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class GoodsDetailForUserVO {

    private Long shopId;

    private String shopName;

    private Long goodsId;
    private String goodsName;
    /** 图片地址 */
    private List<String> pictureAddress;

    private String subTitle;

    /**
     * 规格列表：用于渲染前端的选择器
     * 例如：[{"name": "颜色", "values": ["白色", "黑色"]}, {"name": "内存", "values": ["16g", "32g"]}]
     */
    private List<SpecItem> specList;

    /**
     * SKU 信息列表：包含所有 SKU 的详细数据
     */
    private List<SkuInfo> skuList;
}

