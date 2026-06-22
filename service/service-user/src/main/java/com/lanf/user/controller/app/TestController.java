package com.lanf.user.controller.app;


import com.lanf.common.utils.JsonUtils;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.SendSmsMsg;
import com.lanf.rocketmq.util.MqSendMessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/app/user")
public class TestController {


    @Autowired
    private MqSendMessageUtils  mqSendMessageUtils;

    @Transactional
    @RequestMapping("/test")
    public String test() {
        log.info("测试mq消息发送");

        SendSmsMsg sendSmsMsg = new SendSmsMsg();
        sendSmsMsg.setTemplateCode("SMS_0000000");


        mqSendMessageUtils.sendMessage(TopicName.SEND_SMS_TOPIC, JsonUtils.toJsonString(sendSmsMsg));

        return "success";
    }
}
