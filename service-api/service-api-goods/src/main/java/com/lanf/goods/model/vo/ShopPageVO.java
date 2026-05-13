package com.lanf.goods.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class ShopPageVO implements Serializable {

    /**
     * 店铺名称
     */
    private String name;

    /**
     * 头像
     */
    private String headUrl;

    private Long id;

    private Date createTime;

    private Date updateTime;

    private String createBy;

    private String updateBy;
}
