package com.lanf.pay.model.bo;


import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class TransferBO implements Serializable {

    private String outBizNo;

    private String payeeAccount;

    private String payeeName;

    private BigDecimal amount;

    private String remark;
}
