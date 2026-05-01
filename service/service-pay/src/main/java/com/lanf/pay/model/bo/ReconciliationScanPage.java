package com.lanf.pay.model.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class ReconciliationScanPage implements Serializable {

    private long currentPage = 1;

    private long pageSize = 100;

    private String bathId;

}
