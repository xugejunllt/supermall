package com.lanf.order.model.query;

import com.lanf.constant.model.query.PageQuery;
import lombok.Data;

import java.util.List;


@Data
public class OrderPageQuery extends PageQuery {

    private String searchWord;

    //查询待评价的订单类别
    private Boolean querySubStatus;

    private List<Integer> status;

    private List<Long> orderIdList;


}
