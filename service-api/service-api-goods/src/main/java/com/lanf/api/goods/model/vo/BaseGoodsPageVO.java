package com.lanf.api.goods.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 基础商品分页VO
 */
@Data
public class BaseGoodsPageVO implements Serializable {


    private Long id;
    
    /** 商品编码 */
    private String code;

    /** 商品名称 */
    private String name;

    /** 图片地址，多个,用"，"隔开 */
    private String pictureAddress;

    private Date createTime;

}
