package com.lanf.api.goods.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 基础商品按编码查询VO
 */
@Data
public class BaseGoodsByCodeVO implements Serializable {

    /** 商品名称 */
    private String name;

    private Long goodsId;


    private List<BaseGoodsSkuByCodeQueryVO> baseGoodsSkuByCodeQueryVOList;
}
