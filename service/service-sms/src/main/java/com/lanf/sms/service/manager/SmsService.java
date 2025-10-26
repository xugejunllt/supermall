package com.lanf.sms.service.manager;

import com.lanf.sms.model.bo.SendSmsResultBO;
import com.lanf.sms.model.dto.BathSendSmsDTO;
import com.lanf.sms.model.dto.StartSendSmsDTO;
import com.lanf.sms.model.entity.TemplateDO;
import org.springframework.core.Ordered;

public interface SmsService extends Ordered {

    SendSmsResultBO sendSms(StartSendSmsDTO dto);

    StartSendSmsDTO buildStartSendSmsDTO(TemplateDO one, BathSendSmsDTO dto, String templateParam);

    String getChanel();
}
