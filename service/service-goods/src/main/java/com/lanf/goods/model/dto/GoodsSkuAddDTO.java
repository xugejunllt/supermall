package com.lanf.goods.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class GoodsSkuAddDTO implements Serializable {


    @ApiModelProperty(value = "skuCode")
    private String skuCode;
    @ApiModelProperty(value = "sku图片")
    @NotBlank(message = "sku图片不能为空")
    private String skuPictureAddress;

    private List<SkuNameDTO> skuNameList;

    @ApiModelProperty(value = "价格")
    private BigDecimal price;

    @ApiModelProperty(value = "成本价格")
    private BigDecimal costPrice;

    @ApiModelProperty(value = "商品库存")
    private Integer stock;

    private String skuNameJson;
    private String skuName;

    private Integer sort;

}
