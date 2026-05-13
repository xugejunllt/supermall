package com.lanf.goods.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 基础商品新增DTO
 */
@Data
public class AddBaseGoodsDTO implements Serializable {

    /** 商品名称 */
    private String name;

    /** 图片地址，多个,用"，"隔开 */
    private String pictureAddress;

    private List<List<BaseGoodsSkuAddDTO>> baseGoodsSkuAddList;

}
