package com.lanf.goods.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 商品品牌
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-11
 */
@Data
@TableName("goods_brand")
public class GoodsBrandDO extends BaseEntity {

private static final long serialVersionUID=1L;



    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "排序坐标，越大越靠前")
    private Integer sortIndex;
    private Long shopId;


}
