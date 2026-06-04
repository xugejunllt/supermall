package com.lanf.order.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class SubmitCartVO implements Serializable {


    private String mainOrderNumber;

    private Long mainOrderId;
}
