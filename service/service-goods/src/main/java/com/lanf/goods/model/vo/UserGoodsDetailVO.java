package com.lanf.goods.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class UserGoodsDetailVO implements Serializable {

    private Long id;
    private Long shopId;
    //图片
    private List<String> pictureList;
    //商品名称
    private String goodsName;

    //价格
    private BigDecimal price;

    //属性
    private List<SkuAttributeVO> skuAttributeVOList;

    //属性code -skuCde映射
    private List<UnitCodeSkuCodeVO> unitCodeSkuCodeVOList;

    private List<GoodsSkuVO> goodsSkuVOList;



}

