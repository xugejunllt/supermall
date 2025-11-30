package com.lanf.sms.controller.admin;


import com.lanf.constant.enums.SmsCodeEnum;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.SendSmsMsg;
import com.lanf.rocketmq.util.RocketMqClient;
import com.lanf.sms.service.biz.ITemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

/**
 * <p>
 * 短信模板 前端控制器
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-07-30
 */
@RestController
@RequestMapping("/admin/template")
public class TemplateController {

    @Autowired
    private ITemplateService templateService;
    @Autowired
    private RocketMqClient rocketMqClient;
    @GetMapping("/sendSms")
    public String send() {

        SendSmsMsg dto  = new SendSmsMsg();
        dto.setTemplateCode(SmsCodeEnum.SMS_CODE_1001.getCode());
        dto.setPhone("18320911824");
        dto.setParameterValueList(Arrays.asList("5546"));

        rocketMqClient.sendMessage(TopicName.SEND_SMS_TOPIC,dto);
        return "ok";
    }


}

