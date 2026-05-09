package com.lanf.search.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class OrderSearchVO implements Serializable {

    private Long orderId;

    private Long userId;

    private List<String> goodsName;

}
