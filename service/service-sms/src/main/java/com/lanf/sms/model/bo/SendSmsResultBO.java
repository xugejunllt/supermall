package com.lanf.sms.model.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class SendSmsResultBO implements Serializable {

    private Boolean ok;

    private String failMessage;

}
