package com.lanf.api.goods.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class GoodsCategoryPageVO implements Serializable {

    private Long id;
    
    /** 名称 */
    private String name;

    /** 上级分类id */
    private Long parentId;

    /** 排序坐标，越大越靠前 */
    private Integer sortIndex;
    
    private Integer level;
    
    private List<GoodsCategoryPageVO> children;

}
