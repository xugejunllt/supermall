package com.lanf.goods.model.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class GoodsCategoryPageVO implements Serializable {

    private Long id;
    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "上级分类id")
    private Long parentId;

    @ApiModelProperty(value = "排序坐标，越大越靠前")
    private Integer sortIndex;
    private Integer level;
    private List<GoodsCategoryPageVO> children;

}
