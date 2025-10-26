package com.lanf.goods.model.vo;

import com.lanf.goods.model.entity.BaseGoodsSkuDO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class BaseGoodsSkuGroupVO implements Serializable {

    private String skuName;

    private List<BaseGoodsSkuDO> baseGoodsSkuList;

}
