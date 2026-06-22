package com.lanf.rocketmq.model.message;

import com.lanf.constant.mq.base.BaseMessage;
import lombok.Data;

import java.util.List;

@Data
public class SendSmsMsg extends BaseMessage {



    private String templateCode;

    private String phone;
    //占位符参数值
    private List<String> parameterValueList;


}
