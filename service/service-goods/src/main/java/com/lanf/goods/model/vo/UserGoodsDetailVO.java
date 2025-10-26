package com.lanf.goods.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class UserGoodsDetailVO implements Serializable {

    private Long id;
    private Long shopId;
    //商家id
    private Long businessId;
    //价格
    private BigDecimal price;
    //图片
    private List<String> pictureList;
    //详细名称
    private String detailName;
    //属性
    private List<SkuAttributeVO> skuAttributeVOList;

    private Map<String,Long> skuIdVOMap;




}

