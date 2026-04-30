package com.lanf.pay.mq.message;

import lombok.Data;

import java.io.Serializable;

@Data
public class BillExcelParseRetryMessage implements Serializable {


    private String billType;

    private String billDate;
}
