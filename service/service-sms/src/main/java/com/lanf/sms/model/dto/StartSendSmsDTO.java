package com.lanf.sms.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class StartSendSmsDTO implements Serializable {



    private String templateCode;

    private List<String> phones;
    //json串
    private String templateParam;


}
