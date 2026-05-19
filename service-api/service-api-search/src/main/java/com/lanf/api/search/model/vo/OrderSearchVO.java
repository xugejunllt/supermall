package com.lanf.api.search.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class OrderSearchVO implements Serializable {


    private Long orderId;

    private Long userId;

    private String orderNumber;



    private Integer orderStatus;


    private Date createTime;
    /**
     * 一笔订单 对应多个商品
     */
    private List<String> goodsName;




}
