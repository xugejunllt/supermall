package com.lanf.goods.model.vo;


import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 商品品牌VO
 */
@Data
public class GoodsBrandVO implements Serializable {

    /** 名称 */
    private String name;

    /** 排序坐标，越大越靠前 */
    private Integer sortIndex;

    private Long id;

    private Date createTime;

    private Date updateTime;

    private String createBy;

    private String updateBy;
}
