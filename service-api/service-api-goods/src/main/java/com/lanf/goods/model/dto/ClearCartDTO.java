package com.lanf.goods.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

@Data
public class ClearCartDTO implements Serializable {

    @NotBlank(message = "业务标识不能为空")
    private String bizKeyPrx;
    //购物车id
    @NotEmpty(message = "购物车id不能为空")
    private List<Long> cartIds;

}
