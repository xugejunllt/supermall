package com.lanf.goods.model.bo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ShopGoodsBO implements Serializable {

    private Long shopId;
    //店铺名称
    private String shopName;

    private List<GoodsItemBO> cartItemList;

}
