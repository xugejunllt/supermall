package com.lanf.finance.model.query;

import com.lanf.mybatis.base.PageQuery;
import lombok.Data;

@Data
public class PayAccountPageQuery extends PageQuery {

    private Integer accountType;
}
