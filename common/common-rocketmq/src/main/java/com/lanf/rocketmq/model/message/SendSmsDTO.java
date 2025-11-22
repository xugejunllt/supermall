package com.lanf.rocketmq.model.message;

import com.lanf.messagemanager.client.model.base.BaseMqMessage;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SendSmsDTO extends BaseMqMessage {



    private String templateCode;

    private String phone;
    //占位符参数值
    private List<String> parameterValueList;


}
