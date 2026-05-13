package com.lanf.goods.model.vo;

import com.lanf.goods.model.bo.SkuAttributeBO;
import com.lanf.goods.model.bo.UnitCodeSkuCodeBO;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 用户商品详情VO
 */
@Data
public class UserGoodsDetailVO implements Serializable {

    private Long id;
    
    private Long shopId;
    
    /** 图片 */
    private List<String> pictureList;
    
    /** 商品名称 */
    private String goodsName;

    /** 价格 */
    private BigDecimal price;

    /** 属性 */
    private List<SkuAttributeBO> skuAttributeVOList;

    /** 属性code -skuCde映射 */
    private List<UnitCodeSkuCodeBO> unitCodeSkuCodeVOList;

    private List<GoodsSkuVO> goodsSkuVOList;

}

