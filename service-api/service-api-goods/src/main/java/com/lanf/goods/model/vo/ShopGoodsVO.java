package com.lanf.goods.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ShopGoodsVO implements Serializable {

    private Long shopId;
    //店铺名称
    private String shopName;

    private List<GoodsItemVO> cartItemList;

}
