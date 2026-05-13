package com.lanf.api.goods.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;


@Data
public class AddGoodsBrandDTO implements Serializable {

    @NotBlank(message = "品牌名称不存在")
    private String name;


    @NotNull(message = "排序坐标不能为空")
    private Integer sortIndex;
}
