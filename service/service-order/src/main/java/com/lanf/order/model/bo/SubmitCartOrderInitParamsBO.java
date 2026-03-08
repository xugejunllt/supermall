package com.lanf.order.model.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class SubmitCartOrderInitParamsBO implements Serializable {


    private Long mainOrderId;
    private Long userId;
}
