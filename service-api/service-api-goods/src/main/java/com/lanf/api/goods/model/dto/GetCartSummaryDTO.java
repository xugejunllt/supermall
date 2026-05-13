package com.lanf.api.goods.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

/**
 * 获取购物车摘要DTO
 */
@Data
public class GetCartSummaryDTO implements Serializable {

    /** 购物车id */
    @NotBlank(message = "购物车id不能为空")
    private List<Long> cartIds;

}
