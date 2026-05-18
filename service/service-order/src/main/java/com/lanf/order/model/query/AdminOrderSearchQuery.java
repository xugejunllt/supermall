package com.lanf.order.model.query;

import com.lanf.constant.model.query.PageQuery;
import lombok.Data;

@Data
public class AdminOrderSearchQuery extends PageQuery {

    private String searchWord;

    private String orderNumber;



    private Integer orderStatus;






}
