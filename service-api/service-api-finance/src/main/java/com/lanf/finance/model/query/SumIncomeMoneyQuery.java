package com.lanf.finance.model.query;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class SumIncomeMoneyQuery implements Serializable {

    private Date startTime;

    private Date endTime;
}
