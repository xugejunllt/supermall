package com.lanf.pay.model.query;


import lombok.Data;

import java.io.Serializable;

@Data
public class PaymentSummaryQuery implements Serializable {

    private String batchId;
}
