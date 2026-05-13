package com.lanf.api.goods.model.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.Serializable;

/**
 * 商品分类新增DTO
 */
@Data
public class AddGoodsCategoryDTO implements Serializable {

    /** 名称 */
    private String name;
    
    /** 排序坐标，越大越靠前 */
    @Max(value = 3,message = "超过最大级别")
    @Min(value = 1,message = "小于最小级别")
    private Integer level;


    private Long parentId;

    private Integer sortIndex;

}
