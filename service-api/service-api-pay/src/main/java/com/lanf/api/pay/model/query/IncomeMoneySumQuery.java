package com.lanf.api.pay.model.query;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class IncomeMoneySumQuery implements Serializable {

    private Date startTime;

    private Date endTime;
}
