package com.lanf.api.goods.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-09
 */
@Data
public class BaseGoodsSkuAddDTO implements Serializable {

private static final long serialVersionUID=1L;

    /** sku名称 */
    private String attribute;

    /** sku图片 */
    private String skuPictureAddress;

    /** sku描述 */
    private String attributeDesc;

    /** 排序（影响展示顺序） */
    private Integer sort;

}
