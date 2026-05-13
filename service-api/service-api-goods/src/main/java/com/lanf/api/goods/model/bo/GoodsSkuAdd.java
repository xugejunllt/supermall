package com.lanf.api.goods.model.bo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 商品SKU新增BO
 */
@Data
public class GoodsSkuAdd implements Serializable {


    /** skuCode */
    private String skuCode;
    
    /** sku图片 */
    @NotBlank(message = "sku图片不能为空")
    private String skuPictureAddress;

    private List<SkuName> skuNameList;

    /** 价格 */
    private BigDecimal price;

    /** 成本价格 */
    private BigDecimal costPrice;



    private String skuNameJson;
    
    private String skuName;
    
    /** 0:没有选中 ,1:默认选中 */
    private Integer defaultSelect;
    
    private Integer sort;

}
