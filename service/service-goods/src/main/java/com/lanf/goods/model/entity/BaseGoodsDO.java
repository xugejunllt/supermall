package com.lanf.goods.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import lombok.Data;

/**
 * <p>
 * 基础商品
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-09
 */
@Data
@TableName("base_goods")
public class BaseGoodsDO extends BaseEntity {

private static final long serialVersionUID=1L;


    /** 商品编码 */
    private String code;

    /** 商品名称 */
    private String name;

    /** 图片地址，多个,用"，"隔开 */
    private String pictureAddress;

    private Long  tenantId;

}
