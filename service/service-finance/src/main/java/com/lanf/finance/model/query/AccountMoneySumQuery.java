package com.lanf.finance.model.query;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lanf.common.utils.DateUtils;
import com.lanf.mybatis.base.PageQuery;
import lombok.Data;

import java.util.Date;

@Data
public class AccountMoneySumQuery  {

    //结算完成开始日期（支付完成时间）
    @JsonFormat(pattern = DateUtils.DATE, timezone = "GMT+8")
    private String startTime;
    @JsonFormat(pattern = DateUtils.DATE, timezone = "GMT+8")
    //结算完成结算日期（支付完成时间）
    private String endTime;

    private String incomeAccount;

}
