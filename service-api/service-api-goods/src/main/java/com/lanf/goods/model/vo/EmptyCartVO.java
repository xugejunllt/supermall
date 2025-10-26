package com.lanf.goods.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class EmptyCartVO implements Serializable {

    private List<EmptyCartGoodsSkuVO> goodsSkuVOList;

}
