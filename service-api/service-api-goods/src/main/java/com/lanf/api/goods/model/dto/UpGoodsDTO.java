package com.lanf.api.goods.model.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class UpGoodsDTO implements Serializable {

    @NotNull(message = "商品ID不能为空")
    private Long goodsId;
}
