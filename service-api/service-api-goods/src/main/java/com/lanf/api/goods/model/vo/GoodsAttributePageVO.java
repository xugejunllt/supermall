package com.lanf.api.goods.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class GoodsAttributePageVO implements Serializable {

    /** 属性 */
    private String attribute;

    /** 属性值 多个 用;隔开 */
    private String attributeValue;

    private Integer sort;

    private Long tenantId;

    private Long id;

    private Date createTime;

    private Date updateTime;

    private String createBy;

    private String updateBy;

}
