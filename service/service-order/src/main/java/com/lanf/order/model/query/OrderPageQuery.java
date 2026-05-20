package com.lanf.order.model.query;

import com.lanf.constant.model.query.PageQuery;
import lombok.Data;

import java.util.List;


@Data
public class OrderPageQuery extends PageQuery {

    private List<Integer> status;

    private List<Long> orderIdList;


}
