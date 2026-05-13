package com.lanf.goods.model.dto;

import com.lanf.goods.model.bo.GoodsSkuAddBO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Data
public class GoodsAddDTO implements Serializable {

    @ApiModelProperty(value = "商品编码")
    @NotBlank(message = "商品编码不能为空")
    private String code;

    @ApiModelProperty(value = "商品名称")
    @NotBlank(message = "商品名称不能为空")
    private String name;

    @NotNull(message ="店铺id不能为空" )
    private Long shopId;

    @ApiModelProperty(value = "副标题")
    @NotBlank(message = "副标题不能为空")
    private String title;


    @ApiModelProperty(value = "商品3级分类")
    @NotNull(message ="商品3级分类不能为空" )
    private Long categoryId;

    @ApiModelProperty(value = "品牌")
    @NotNull(message ="品牌不能为空" )
    private Long brandId;

    //搜索提示词标签
    @NotEmpty(message = "搜索提示词标签不能为空")
    //多个 用","隔开
    private String promptWordLabel;
    //扩展标签 用于展示
    //多个 用","隔开
    @NotEmpty(message = "扩展标签不能为空")
    private String extendedTags;

    @NotEmpty(message = "商品sku不能为空")
    private List<GoodsSkuAddBO> goodsSkuAddDTOList;
    @NotEmpty(message = "商品图片不能为空")
    private   List<String>  pictureAddressList;


}
