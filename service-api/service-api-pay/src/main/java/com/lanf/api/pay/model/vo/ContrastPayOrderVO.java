package com.lanf.api.pay.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class ContrastPayOrderVO implements Serializable {

    private Long payOrderId;

    private String errorMsg;
}
