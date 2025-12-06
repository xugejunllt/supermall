package com.lanf.search.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 商品同步es的数据
 * </p>
 *
 * @author jarven
 * @since 2025-12-06
 */
@Data
@TableName("goods_info")
public class GoodsInfoDO implements Serializable {

private static final long serialVersionUID=1L;


    @ApiModelProperty(value = "商品id")
    private Long goodsId;

    @ApiModelProperty(value = "商品json信息")
    private String goodsInfo;

    @ApiModelProperty(value = "版本号")
    private Long version;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    @TableField( fill = FieldFill.INSERT)
    private Date createTime;





}
