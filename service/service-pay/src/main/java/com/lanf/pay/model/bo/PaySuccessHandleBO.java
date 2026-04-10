package com.lanf.pay.model.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class PaySuccessHandleBO implements Serializable {

    private Integer payType;

    private  CallbackResultBO resultBO;




}
