package com.lanf.pay.model.dto;

import lombok.Data;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;

@Data
public class PayCallbackDTO implements Serializable {


    //支付方式
    private Integer payType;

    private HttpServletRequest request;

    private HttpServletResponse response;

}
