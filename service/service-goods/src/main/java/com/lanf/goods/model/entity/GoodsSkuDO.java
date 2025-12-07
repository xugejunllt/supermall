package com.lanf.goods.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * <p>
 * 
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-11
 */
@Data
@TableName("goods_sku")
public class GoodsSkuDO extends BaseEntity {

private static final long serialVersionUID=1L;


    private Long goodsId;

    @ApiModelProperty(value = "skuCode")
    private String skuCode;

    @ApiModelProperty(value = "sku名称json")
    private String skuNameJson;

    private String skuName;

    @ApiModelProperty(value = "sku图片")
    private String skuPictureAddress;

    @ApiModelProperty(value = "价格")
    private BigDecimal price;

    @ApiModelProperty(value = "成本价格")
    private BigDecimal costPrice;
    //0:没有选中 ,1:默认选中
    private Integer defaultSelect;

    private Integer sort;

    private Long version;


}
