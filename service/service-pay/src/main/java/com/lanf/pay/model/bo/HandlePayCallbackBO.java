package com.lanf.pay.model.bo;

import lombok.Data;

import java.io.Serializable;
@Data
public class HandlePayCallbackBO implements Serializable {

    private boolean responseOk;

    private Exception exception;

}
