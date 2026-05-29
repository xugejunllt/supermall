package com.lanf.api.pay.model.query;

import com.lanf.constant.model.query.PageQuery;
import lombok.Data;

@Data
public class PayAccountPageQuery extends PageQuery {

    private Integer accountType;
}
