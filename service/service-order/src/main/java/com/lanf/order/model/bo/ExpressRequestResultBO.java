package com.lanf.order.model.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class ExpressRequestResultBO implements Serializable {


    private String returnCode;

    private String message;

    private Boolean result;
}
