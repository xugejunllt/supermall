package com.lanf.api.goods.model.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 清空购物车DTO
 */
@Data
public class ClearCartDTO implements Serializable {

    @NotNull(message = "用户id不能为空")
    private Long userId;

    /** 购物车id */
    @NotEmpty(message = "购物车id不能为空")
    private List<Long> cartIds;

}
