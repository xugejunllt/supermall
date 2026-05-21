package com.lanf.finance.model.query;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lanf.common.utils.DateUtils;
import com.lanf.constant.model.query.PageQuery;
import lombok.Data;

@Data
public class MoneyFlowPageQuery extends PageQuery {

    //结算完成开始日期（支付完成时间）
    @JsonFormat(pattern = DateUtils.DATE, timezone = "GMT+8")
    private String startTime;
    @JsonFormat(pattern = DateUtils.DATE, timezone = "GMT+8")
    //结算完成结算日期（支付完成时间）
    private String endTime;


}
