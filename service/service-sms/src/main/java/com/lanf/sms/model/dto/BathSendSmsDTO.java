package com.lanf.sms.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class BathSendSmsDTO implements Serializable {



    private String templateCode;

    private List<String> phones;
    //占位符参数值
    private List<String> parameterValueList;


}
