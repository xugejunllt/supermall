package com.lanf.api.goods.model.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * 验证购物车DTO
 */
@Data
public class ValidateCartDTO implements Serializable {

    /** 购物车id */
    @NotEmpty(message = "购物车id不能为空")
    private List<Long> cartIds;

    private Long userId;

}
