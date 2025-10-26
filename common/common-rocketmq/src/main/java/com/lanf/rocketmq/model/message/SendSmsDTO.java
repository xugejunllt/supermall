package com.lanf.rocketmq.model.message;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SendSmsDTO implements Serializable {



    private String templateCode;

    private String phone;
    //占位符参数值
    private List<String> parameterValueList;


}
