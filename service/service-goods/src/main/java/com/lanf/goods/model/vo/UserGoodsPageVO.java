package com.lanf.goods.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 用户商品分页VO
 */
@Data
public class UserGoodsPageVO implements Serializable {

    private Long id;
    
    /** 标题 */
    private String name;
    
    /** 价格 */
    private BigDecimal price;
    
    /** 图片 */
    private String picture;

    private Long shopId;

}

