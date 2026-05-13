package com.lanf.goods.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class GoodsPageVO implements Serializable {


    private Long id;

    /** 商品编码 */
    private String code;

    /** 商品名称 */
    private String name;

    /** 上下架状态 0:上架 ,1:下架 */
    private Integer upDownStatus;

    private Date createTime;
}
