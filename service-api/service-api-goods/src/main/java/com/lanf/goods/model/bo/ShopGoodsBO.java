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

    /**
     * 订单ID
     * 不是接口返回 的字段
     * 在提交购物车聚合方法里初始化
     */
    private Long orderId;
}
