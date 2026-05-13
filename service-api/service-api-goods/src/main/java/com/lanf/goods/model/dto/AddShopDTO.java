package com.lanf.goods.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 店铺DTO
 */
@Data
public class AddShopDTO implements Serializable {


    /** 店铺名称 */
    private String name;

    /** 头像 */
    private String headUrl;

}
