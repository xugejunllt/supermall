package com.lanf.pay.model.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class PaySuccessHandleResultBO implements Serializable {

    private  Boolean responseOk;

    public PaySuccessHandleResultBO(Boolean responseOk) {
        this.responseOk = responseOk;
    }
}
