package com.lanf.goods.model.bo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class GoodsSkuAddBO implements Serializable {


    @ApiModelProperty(value = "skuCode")
    private String skuCode;
    @ApiModelProperty(value = "sku图片")
    @NotBlank(message = "sku图片不能为空")
    private String skuPictureAddress;

    private List<SkuNameBO> skuNameList;

    @ApiModelProperty(value = "价格")
    private BigDecimal price;

    @ApiModelProperty(value = "成本价格")
    private BigDecimal costPrice;



    private String skuNameJson;
    private String skuName;
    //0:没有选中 ,1:默认选中
    private Integer defaultSelect;
    private Integer sort;

}
