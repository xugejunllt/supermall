package com.lanf.goods.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 基础商品按编码查询VO
 */
@Data
public class BaseGoodsByCodeQueryVO implements Serializable {

    /** 商品名称 */
    private String name;

    /** 多个属性名用","分隔 */
    private String attributeSplit;

    private List<BaseGoodsSkuByCodeQueryVO> baseGoodsSkuByCodeQueryVOList;
}
