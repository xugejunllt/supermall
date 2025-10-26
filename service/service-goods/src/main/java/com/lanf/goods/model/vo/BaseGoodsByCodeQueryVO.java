package com.lanf.goods.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class BaseGoodsByCodeQueryVO implements Serializable {

    @ApiModelProperty(value = "商品名称")
    private String name;

    //多个属性名用","分隔
    private String attributeSplit;

    private List<BaseGoodsSkuByCodeQueryVO> baseGoodsSkuByCodeQueryVOList;
}
