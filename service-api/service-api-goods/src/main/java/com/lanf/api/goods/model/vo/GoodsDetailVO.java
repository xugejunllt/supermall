package com.lanf.api.goods.model.vo;

import com.lanf.api.goods.model.bo.GoodsSkuDetail;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 商品详情VO
 */
@Data
public class GoodsDetailVO implements Serializable {


    private Long id;

    /** 商品编码 */
    private String code;

    /** 商品名称 */
    private String name;

    /** 副标题 */
    private String title;

    /** 图片地址，多个,用"，"隔开 */
    private String pictureAddress;

    /** 商品3级分类 */
    private String categoryName;

    /** 品牌 */
    private String brandName;

    /** 上下架状态 0:下架 ,1:上架 */
    private Integer upDownStatus;


    private Long shopId;
    /** 店铺名称 */
    private String shopName;

    /** 搜索提示词标签 */
    private List<String> promptWordLabel;

    /** 扩展标签 用于展示 */
    private List<String> extendedTags;


    private List<GoodsSkuDetail> goodsSkuDetailVOList;



}
